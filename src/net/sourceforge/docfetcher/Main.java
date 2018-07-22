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

package net.sourceforge.docfetcher;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.docfetcher.util.AppUtil;
import net.sourceforge.docfetcher.util.ClassPathHack;
import net.sourceforge.docfetcher.util.SwtJarLocator;

import com.google.common.base.Joiner;

/**
 * @author Tran Nam Quang
 */
public final class Main {

	private Main() {
	}

	public static void main(String[] args) throws Exception {
		/*
		 * For DocFetcher to start up, the correct SWT jar has to be added to
		 * the classpath. Which SWT jar is correct depends on the platform
		 * (i.e., Windows / Linux / OS X, and 32-bit vs. 64-bit). In the
		 * following, the correct SWT jar is first determined and then either
		 * loaded dynamically using a classpath hack, or by launching a new Java
		 * process with the SWT jar on the classpath. The second approach is
		 * needed for Java 9 and later, where the classpath hack doesn't work
		 * anymore.
		 */
		if (isJava9OrLater()) {
			// Collect jar paths
			List<String> jarPaths = new ArrayList<String>();
			File libDir = new File("../Resources/lib"); // non-portable OS X version?
			if (!libDir.isDirectory()) {
				libDir = new File("lib");
			}
			for (File file : libDir.listFiles()) {
				if (file.getName().toLowerCase().endsWith(".jar")) {
					jarPaths.add(file.getPath());
				}
			}
			jarPaths.add(SwtJarLocator.getFile().getPath());
			
			// Add language files to the classpath
			File langDir = new File("dist/lang"); // running inside the IDE?
			if (langDir.isDirectory()) {
				jarPaths.add(langDir.getPath());
			}
			else {
				langDir = new File("../Resources/lang"); // non-portable OS X version
				if (langDir.isDirectory()) {
					jarPaths.add(langDir.getPath());
				}
				else {
					langDir = new File("lang"); // all other versions
					if (langDir.isDirectory()) {
						jarPaths.add(langDir.getPath());
					}
				}
			}
			
			// Building classpath
			String osName = System.getProperty("os.name").toLowerCase();
			boolean isWindows = osName.contains("windows");
			boolean isOSX = osName.equals("mac os x");
			String classSep = isWindows ? ";" : ":";
			String classPath = Joiner.on(classSep).join(jarPaths);
			if (!isWindows) {
				classPath = ".:" + classPath;
			}
			
			// Collecting command-line arguments
			List<String> cmdList = new ArrayList<String>();
			if (isOSX) {
				cmdList.add("/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java");
				cmdList.add("-XstartOnFirstThread");
				cmdList.add("-Xdock:name=\"DocFetcher\"");
			}
			else {
				cmdList.add("java");
			}
			cmdList.add("-enableassertions");
			cmdList.add(getMaxHeapSizeStr());
			cmdList.add(getStackSizeStr());
			cmdList.add("-cp");
			cmdList.add(classPath);
			cmdList.add(String.format("-Djava.library.path=%s", libDir.getPath()));
			cmdList.add("-Duser.language=" + System.getProperty("user.language"));
			cmdList.add("net.sourceforge.docfetcher.gui.Application");
			for (String arg : args) {
				cmdList.add(arg);
			}
			
			// Launch new Java process
			String[] cmdArr = cmdList.toArray(new String[cmdList.size()]);
			Runtime.getRuntime().exec(cmdArr);
		}
		else {
			ClassPathHack.addFile(SwtJarLocator.getFile());
			ClassPathHack.addFile(AppUtil.getLangDir());
			
			String appClassName = "net.sourceforge.docfetcher.gui.Application";
			Class<?> appClass = Class.forName(appClassName);
			Class<?>[] paramTypes = new Class<?>[] {String[].class};
			Method launchMethod = appClass.getMethod("main", paramTypes);
			launchMethod.invoke(null, new Object[] {args});
		}
	}
	
	private static boolean isJava9OrLater() {
		/*
		 * Java 9 and later are numbered 9, 10, etc., whereas earlier versions
		 * started with "1.", e.g. 1.5, 1.6, 1.7, etc.
		 */
		String versionStr = System.getProperty("java.version");
		Matcher m = Pattern.compile("(\\d+).*").matcher(versionStr);
		if (!m.matches()) {
			return false;
		}
		return Integer.parseInt(m.group(1)) > 1;
	}
	
	private static String getMaxHeapSizeStr() {
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		List<String> args = bean.getInputArguments();
		Pattern pat = Pattern.compile("-Xmx\\d+.*");
		for (int i = 0; i < args.size(); i++) {
			if (pat.matcher(args.get(i)).matches()) {
				return args.get(i);
			}
		}
		return "-Xmx512m";
	}
	
	private static String getStackSizeStr() {
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		List<String> args = bean.getInputArguments();
		Pattern pat = Pattern.compile("-Xss\\d+.*");
		for (int i = 0; i < args.size(); i++) {
			if (pat.matcher(args.get(i)).matches()) {
				return args.get(i);
			}
		}
		return "-Xss2m";
	}

}
