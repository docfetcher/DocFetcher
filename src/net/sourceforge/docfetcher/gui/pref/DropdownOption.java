/*******************************************************************************
 * Copyright (c) 2018 Tran Nam Quang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tran Nam Quang - initial API and implementation
 *******************************************************************************/

package net.sourceforge.docfetcher.gui.pref;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.sourceforge.docfetcher.enums.SettingsConf;
import net.sourceforge.docfetcher.gui.pref.PrefDialog.PrefOption;
import net.sourceforge.docfetcher.util.annotations.NotNull;

/**
 * @author Tran Nam Quang
 */
final class DropdownOption extends PrefOption {
	
	@NotNull private SettingsConf.Int enumOption;
	@NotNull private CCombo dropdown;
	@NotNull private String[] choices;
	
	public DropdownOption(	@NotNull String labelText,
							@NotNull SettingsConf.Int enumOption,
							@NotNull String[] choices) {
		super(labelText);
		this.enumOption = enumOption;
		this.choices = choices;
	}

	@Override
	protected void createControls(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(labelText);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		int style = SWT.BORDER | SWT.READ_ONLY | SWT.FLAT;
		dropdown = new CCombo(parent, style);
		dropdown.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		dropdown.setCursor(dropdown.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		dropdown.setItems(choices);
		dropdown.setText(choices[enumOption.get()]);
	}

	@Override
	protected void restoreDefault() {
		dropdown.setText(choices[enumOption.get()]);
	}

	@Override
	protected void save() {
		int index = 0;
		for (int i = 0; i < choices.length; i++) {
			if (choices[i].equals(dropdown.getText())) {
				index = i;
				break;
			}
		}
		enumOption.set(index);
	}

}
