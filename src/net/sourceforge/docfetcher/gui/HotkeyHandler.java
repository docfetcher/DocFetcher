/*******************************************************************************
 * Copyright (c) 2018 Tran Nam Quang
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tran Nam Quang - removed some external dependencies; minor tweaks
 *******************************************************************************/

package net.sourceforge.docfetcher.gui;

import java.io.File;

import org.eclipse.swt.SWT;

import com.melloware.jintellitype.JIntellitype;

import jxgrabkey.HotkeyConflictException;
import jxgrabkey.JXGrabKey;
import net.sourceforge.docfetcher.enums.SystemConf;
import net.sourceforge.docfetcher.util.Event;
import net.sourceforge.docfetcher.util.Util;

/**
 * @author Tran Nam Quang
 */
public final class HotkeyHandler {
	
	private static final int HOTKEY_ID = 1;
	
	public final Event<Void> evtHotkeyPressed = new Event<Void> ();
	
	private final HotkeyListenerImpl impl;
	
	/*
	 * On Windows with Java 9 and later, registering the hotkey, unregistering
	 * the hotkey and shutting down the hotkey implementation crash the VM.
	 * We'll use this flag to avoid crashing when the unregister and shutdown
	 * methods are called.
	 */
	private volatile boolean wasRegistered = false;
	
	public HotkeyHandler() {
		if (Util.IS_WINDOWS)
			impl = new HotkeyListenerWindowsImpl();
		else if (Util.IS_LINUX)
			impl = new HotkeyListenerLinuxImpl();
		else
			throw new UnsupportedOperationException();
	}
	
	public boolean registerHotkey(int mask, int key) {
		if (!wasRegistered) {
			impl.init(this);
			wasRegistered = true;
		}
		return impl.registerHotkey(HOTKEY_ID, mask, key);
	}
	
	public void unregisterHotkey() {
		if (wasRegistered) {
			impl.unregisterHotkey(HOTKEY_ID);
		}
	}
	
	public void shutdown() {
		if (wasRegistered) {
			impl.unregisterHotkey(HOTKEY_ID);
			impl.shutdown();
		}
	}

	void onHotKey(int hotkey_id) {
		if (hotkey_id != HOTKEY_ID)
			return;
		evtHotkeyPressed.fire(null);
	}
	
}

interface HotkeyListenerImpl {
	public void init(HotkeyHandler handler);
	public boolean registerHotkey(int id, int mask, int key);
	public void unregisterHotkey(int id);
	public void shutdown();
}

final class HotkeyListenerWindowsImpl implements HotkeyListenerImpl {
	public void init(final HotkeyHandler listener) {
		boolean isDev = SystemConf.Bool.IsDevelopmentVersion.get();
		int arch = Util.IS_64_BIT_JVM ? 64 : 32;
		
		String libPath = isDev ? "lib/jintellitype" : "lib";
		libPath = String.format("%s/JIntellitype%d.dll", libPath, arch);
		JIntellitype.setLibraryLocation(libPath);

		JIntellitype.getInstance().addHotKeyListener(
			new com.melloware.jintellitype.HotkeyListener() {
				public void onHotKey(int hotkey_id) {
					listener.onHotKey(hotkey_id);
				}
			}
		);
	}

	public boolean registerHotkey(int id, int mask, int key) {
		/*
		 * Bug in JIntellitype 1.3.7: The method
		 * JIntellitype.registerSwingHotKey incorrectly translates AWT
		 * modifiers to JIntellitype modifiers. The fix is to translate
		 * directly from SWT modifiers to JIntellitype modifiers.
		 */
		JIntellitype.getInstance().registerHotKey(id,
			toIntellitypeModifier(mask),
			KeyCodeTranslator.translateSWTKey(key)
		);
		return true;
	}

	public void unregisterHotkey(int id) {
		JIntellitype.getInstance().unregisterHotKey(id);
	}

	public void shutdown() {
		JIntellitype.getInstance().cleanUp();
	}
	
	private int toIntellitypeModifier(int swtModifier) {
		int mask = 0;
		if ((swtModifier & SWT.CTRL) != 0)
			mask |= JIntellitype.MOD_CONTROL;
		if ((swtModifier & SWT.ALT) != 0)
			mask |= JIntellitype.MOD_ALT;
		if ((swtModifier & SWT.SHIFT) != 0)
			mask |= JIntellitype.MOD_SHIFT;
		return mask;
	}
}

final class HotkeyListenerLinuxImpl implements HotkeyListenerImpl {
	public void init(final HotkeyHandler listener) {
		boolean isDev = SystemConf.Bool.IsDevelopmentVersion.get();
		int arch = Util.IS_64_BIT_JVM ? 64 : 32;
		
		String libPath = isDev ? "lib/jxgrabkey" : "lib";
		libPath = String.format("%s/libJXGrabKey%d.so", libPath, arch);
		System.load(Util.getAbsPath(new File(libPath)));
		
		JXGrabKey.getInstance().addHotkeyListener(new jxgrabkey.HotkeyListener(){
			public void onHotkey(int hotkey_id) {
				listener.onHotKey(hotkey_id);
			}
        });
	}

	public boolean registerHotkey(int id, int mask, int key) {
		try {
			JXGrabKey.getInstance().registerAwtHotkey(id,
					KeyCodeTranslator.translateSWTModifiers(mask),
					KeyCodeTranslator.translateSWTKey(key));
			return true;
		}
		catch (HotkeyConflictException e) {
			return false;
		}
	}
	
	public void unregisterHotkey(int id) {
		JXGrabKey.getInstance().unregisterHotKey(id);
	}

	public void shutdown() {
        JXGrabKey.getInstance().cleanUp();
	}
}