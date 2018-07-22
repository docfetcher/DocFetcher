/*******************************************************************************
 * Copyright (c) 2011 Tran Nam Quang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tran Nam Quang - initial API and implementation
 *******************************************************************************/

package net.sourceforge.docfetcher.gui.indexing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import net.sourceforge.docfetcher.enums.Img;
import net.sourceforge.docfetcher.enums.Msg;
import net.sourceforge.docfetcher.enums.ProgramConf;
import net.sourceforge.docfetcher.enums.SettingsConf;
import net.sourceforge.docfetcher.model.LuceneIndex;
import net.sourceforge.docfetcher.model.index.PatternAction;
import net.sourceforge.docfetcher.model.index.PatternAction.MatchAction;
import net.sourceforge.docfetcher.model.index.PatternAction.MatchTarget;
import net.sourceforge.docfetcher.model.index.file.FileIndex;
import net.sourceforge.docfetcher.util.AppUtil;
import net.sourceforge.docfetcher.util.Util;
import net.sourceforge.docfetcher.util.annotations.MutableCopy;
import net.sourceforge.docfetcher.util.annotations.NotNull;
import net.sourceforge.docfetcher.util.gui.LazyImageCache;
import net.sourceforge.docfetcher.util.gui.viewer.ColumnEditSupport;
import net.sourceforge.docfetcher.util.gui.viewer.ColumnEditSupport.ComboEditSupport;
import net.sourceforge.docfetcher.util.gui.viewer.ColumnEditSupport.TextEditSupport;
import net.sourceforge.docfetcher.util.gui.viewer.SimpleTableViewer;
import net.sourceforge.docfetcher.util.gui.viewer.SimpleTableViewer.Column;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * @author Tran Nam Quang
 */
final class PatternTable extends Composite {
	
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		LazyImageCache lazyImageCache = new LazyImageCache(display, "dist/img");
		Img.initialize(lazyImageCache);
		AppUtil.Const.autoInit();
		
		FileIndex index = new FileIndex(null, new File(""));
		PatternTable patternTable = new PatternTable(shell, index);
		patternTable.setStoreRelativePaths(index.getConfig().isStoreRelativePaths());

		Util.setCenteredBounds(shell);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
	
	private final LuceneIndex index;
	@NotNull private SimpleTableViewer<PatternAction> tableViewer;
	private final RegexTestPanel regexTestPanel;
	private boolean storeRelativePaths;
	
	public PatternTable(@NotNull Composite parent,
						@NotNull LuceneIndex index) {
		super(parent, SWT.NONE);
		this.index = index;
		setLayout(Util.createGridLayout(2, false, 0, 5));
		
		Table table = createTable();
		Control buttonPanel = createButtonPanel();
		
		regexTestPanel = new RegexTestPanel(this, index);
		regexTestPanel.setStoreRelativePaths(storeRelativePaths);
		
		GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		int factor = ProgramConf.Int.PatternTableHeight.get() + 1; // +1 for column header
		tableGridData.minimumHeight = Math.max(table.getItemHeight() * factor + 5, 120);
		table.setLayoutData(tableGridData);
		
		buttonPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 2));
		regexTestPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		restoreDefaults();
	}
	
	@NotNull
	private Table createTable() {
		/*
		 * Note: The table has SWT.SINGLE style because moving more than one
		 * element up or down at once is currently not supported.
		 */
		int style = SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION;
		tableViewer = new SimpleTableViewer<PatternAction>(this, style);
		tableViewer.enableEditSupport();
		
		tableViewer.addColumn(new Column<PatternAction>(Msg.pattern_regex.get()) {
			protected String getLabel(PatternAction element) {
				return element.getRegex();
			}
			protected ColumnEditSupport<PatternAction> getEditSupport() {
				return new TextEditSupport<PatternAction>() {
					protected void setText(PatternAction element, String text) {
						element.setRegex(text);
						updateRegexTestPanel();
					}
				};
			}
		});
		
		tableViewer.addColumn(new Column<PatternAction>(Msg.match_against.get()) {
			protected String getLabel(PatternAction element) {
				return getLabel(element.getTarget());
			}
			protected ColumnEditSupport<PatternAction> getEditSupport() {
				return new ComboEditSupport<PatternAction, MatchTarget>(MatchTarget.class) {
					protected void setChoice(	PatternAction element,
												MatchTarget target) {
						element.setTarget(target);
						updateRegexTestPanel();
					}
					protected String toString(MatchTarget enumInstance) {
						return getLabel(enumInstance);
					}
				};
			}
			private String getLabel(MatchTarget target) {
				switch (target) {
				case FILENAME: return Msg.filename.get();
				case PATH:
					return storeRelativePaths
						? Msg.relative_path.get()
						: Msg.absolute_path.get();
				}
				throw new IllegalStateException();
			}
		});
		
		tableViewer.addColumn(new Column<PatternAction>(Msg.action.get()) {
			protected String getLabel(PatternAction element) {
				switch (element.getAction()) {
				case EXCLUDE: return Msg.exclude.get();
				case DETECT_MIME: return Msg.detect_mime_type.get();
				}
				throw new IllegalStateException();
			}
			protected ColumnEditSupport<PatternAction> getEditSupport() {
				return new ComboEditSupport<PatternAction, MatchAction>(MatchAction.class) {
					protected void setChoice(	PatternAction element,
												MatchAction action) {
						element.setAction(action);
						updateRegexTestPanel();
					}
					protected String toString(MatchAction enumInstance) {
						return enumInstance.displayName;
					}
				};
			}
		});
		
		Table table = tableViewer.getControl();
		table.setLinesVisible(true);
		SettingsConf.ColumnWidths.PatternTable.bind(table);
		
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateRegexTestPanel();
			}
		});
		
		return table;
	}
	
	private void updateRegexTestPanel() {
		regexTestPanel.setPatternActions(tableViewer.getSelection());
	}
	
	@NotNull
	private Control createButtonPanel() {
		Composite comp = new Composite(this, SWT.NONE);
		comp.setLayout(Util.createGridLayout(1, false, 0, 5));
		
		Util.createPushButton(
			comp, Img.ADD.get(), Msg.add_pattern.get(), new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PatternAction patternAction = new PatternAction();
				tableViewer.add(patternAction);
				tableViewer.showElement(patternAction);
				tableViewer.setSelection(patternAction);
				updateRegexTestPanel();
			}
		});
		
		Util.createPushButton(
			comp, Img.REMOVE.get(), Msg.remove_sel_pattern.get(),
			new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (PatternAction patternAction : tableViewer.getSelection())
					tableViewer.remove(patternAction);
				updateRegexTestPanel();
			}
		});
		
		Util.createPushButton(
			comp, Img.ARROW_UP.get(), Msg.increase_pattern_priority.get(), new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				List<PatternAction> sel = tableViewer.getSelection();
				if (sel.size() == 1)
					tableViewer.move(sel.get(0), true);
			}
		});
		
		Util.createPushButton(
			comp, Img.ARROW_DOWN.get(), Msg.decrease_pattern_priority.get(), new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				List<PatternAction> sel = tableViewer.getSelection();
				if (sel.size() == 1)
					tableViewer.move(sel.get(0), false);
			}
		});
		final List<String> listFiles = getTemplates("templates");
		if (listFiles.size() > 0)
			Util.createPushButton(
				comp, Img.STAR.get(), Msg.add_pattern_from_template.get(), new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					showTemplateMenu(listFiles, "templates");
				}
			});
		return comp;
	}
	
	private void showTemplateMenu(List<String> listFiles, String dirPath) {
		Menu myMenu = new Menu(this);
		for (int i = 0; i < listFiles.size(); i++) {
			MenuItem testItem = new MenuItem(myMenu, SWT.PUSH);			
			testItem.setText(listFiles.get(i).replaceAll(".xml", ""));
			final String fileName = (new File(dirPath, listFiles.get(i))).getPath();
			testItem.addSelectionListener(new SelectionAdapter() {
			    @Override
			    public void widgetSelected(SelectionEvent e) {
			    	loadFromFile(fileName, false);
			    }
			});    	
	    }		
		myMenu.setVisible(true);        		
	}
	
	private List<String> getTemplates(String dirPath) {
		List<String> listFiles = new ArrayList<String>();
		for (File file : Util.listFiles(new File(dirPath))) {
			if (file.getName().endsWith(".xml"))
				listFiles.add(file.getName());
		}
		return listFiles;
	}
	
	public void setStoreRelativePaths(boolean storeRelativePaths) {
		this.storeRelativePaths = storeRelativePaths;
		regexTestPanel.setStoreRelativePaths(storeRelativePaths);
		for (PatternAction patternAction : tableViewer.getElements())
			tableViewer.update(patternAction);
	}
	
	@MutableCopy
	@NotNull
	public List<PatternAction> getPatternActions() {
		return tableViewer.getElements();
	}
	
	public void restoreDefaults() {
		tableViewer.removeAll();
		for (PatternAction patternAction : index.getConfig().getPatternActions())
			tableViewer.add(patternAction);
		updateRegexTestPanel();
	}
	
	
	public void loadFromFile(@NotNull String pathname, boolean doRemoveAll) {
		try {
			File fXmlFile = new File(pathname);
			if (fXmlFile.exists()) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXmlFile);
				doc.getDocumentElement().normalize();
				
				if (doRemoveAll) tableViewer.removeAll();
				NodeList nList = doc.getElementsByTagName("pattern");
				for (int temp = 0; temp < nList.getLength(); temp++) {
					Node nNode = nList.item(temp);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						PatternAction myPattern = new PatternAction(getTagValue("regex", eElement));
						String strElem = getTagValue("target", eElement).toUpperCase();
						if (strElem.equals("FILENAME")) {
							myPattern.setTarget(MatchTarget.FILENAME);
						} else if (strElem.equals("PATH")) {
							myPattern.setTarget(MatchTarget.PATH);
						}
						strElem = getTagValue("action", eElement).toUpperCase();
						if (strElem.equals("EXCLUDE")) {
							myPattern.setAction(MatchAction.EXCLUDE);
						} else if (strElem.equals("DETECT_MIME")) {
							myPattern.setAction(MatchAction.DETECT_MIME);
						}
						tableViewer.add(myPattern);					
					}
				}			
				updateRegexTestPanel();
			}
		} catch (Exception e) {
			AppUtil.showStackTrace(e);
		}
	}
	
	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();	
		Node nValue = (Node) nlList.item(0);
		return nValue.getNodeValue();
	}
	
}
