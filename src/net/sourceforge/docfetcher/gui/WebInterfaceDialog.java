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

package net.sourceforge.docfetcher.gui;

import net.sourceforge.docfetcher.enums.Msg;
import net.sourceforge.docfetcher.util.Util;
import net.sourceforge.docfetcher.util.annotations.NotNull;
import net.sourceforge.docfetcher.util.gui.FormDataFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Tran Nam Quang
 */
final class WebInterfaceDialog {

	public static void main(String[] args) {
		Display display = new Display();
		final Shell shell = new Shell(display);
		Util.setCenteredBounds(shell, 200, 100);
		shell.setLayout(new GridLayout());
		
		Button bt = new Button(shell, SWT.PUSH);
		bt.setText("Open Dialog");
		bt.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		bt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				WebInterfaceDialog dialog = new WebInterfaceDialog(shell);
				dialog.open();
			}
		});

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
	
	private final Shell shell;
	
	public WebInterfaceDialog(@NotNull Shell parentShell) {
		shell = new Shell(parentShell, SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
		shell.setLayout(Util.createFormLayout(5));
		shell.setText(Msg.web_interface.get());
		
		Composite comp = new Composite(shell, SWT.NONE);
		comp.setLayout(new RowLayout());
		
		Button enableButton = new Button(comp, SWT.CHECK);
		enableButton.setText(Msg.enable_web_interface.get());
		
		enableButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				/*
				 * TODO web interface: start or stop web interface
				 * TODO web interface: also stop web interface automatically when the program exits (in Main class)
				 * TODO web interface: remove comment in ProgramConf.Int.WebInterfacePageSize.
				 */
			}
		});
		
		Button closeButton = new Button(shell, SWT.PUSH);
		closeButton.setText(Msg.close.get());
		closeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		
		FormDataFactory fdf = FormDataFactory.getInstance();
		fdf.bottom().right().minWidth(Util.BTW).applyTo(closeButton);
		fdf.reset().bottom(closeButton).left().top().right().applyTo(comp);
	}
	
	public void open() {
		Util.setCenteredBounds(shell, 300, 200);
		shell.open();
	}

}
