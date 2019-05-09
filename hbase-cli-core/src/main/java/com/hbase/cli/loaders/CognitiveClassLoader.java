package com.hbase.cli.loaders;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hbase.cli.constants.CoreConstants;

/**
 * Adds the expanded URLs to the classloader, but done in a cognitive way
 *
 */
public class CognitiveClassLoader {

	private static final NotificationLogger CognitiveClassLoaderLogger = NotificationLoggerFactory.getLogger();

	private static final String LOGGING_CLASS_NAME = CognitiveClassLoader.class.getName();

	private String javaClassPathProperty = "";
	private static CognitiveClassLoader loaderInstance;
	private URLClassLoader loader;
	private List<String> pathCache = new ArrayList<String>();
	private List<URL> urlCache = new ArrayList<URL>();

	// Restricting instantiation
	private CognitiveClassLoader() {
	}

	/**
	 * Constructs URLClassloader using classpath
	 * 
	 * @param classPath
	 * @return
	 * @throws MalformedURLException
	 */
	public static ClassLoader getClassLoader(String classPath) throws MalformedURLException, Exception {
		if (loaderInstance == null) {
			loaderInstance = new CognitiveClassLoader();
		}

		return loaderInstance.createURLClassLoader(classPath);
	}

	/*
	 * Loads the URLs only if not found in the loader previously
	 */
	private ClassLoader createURLClassLoader(String classPath) throws MalformedURLException,  Exception {
		
		if (loader == null) {
			loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			addToJavaClassPath(loader);
		}

		if (classPath!=null && !classPath.isEmpty()) {
			List<String> urlList = parseClasspath(classPath);
			if (!urlList.isEmpty()) {
				URL[] finalURLs;
				URLClassLoader codecClasses;
				Class<URLClassLoader> urlClass;
				List<File> unloadedDirectories = new ArrayList<File>();
				
				for (String url : urlList) {

					File file = new File(url);

					// Checking for valid path
					if (file.exists()) {

						// Checking for directory
						if (file.isDirectory()) {
							if (!pathCache.contains(url)) {
								pathCache.add(url);
								unloadedDirectories.add(file);
							}

						} else if (url.contains(CoreConstants.ENDS_WITH_JAR)) {
							urlCache.add(file.toURI().toURL());
						}
					} else {
						CognitiveClassLoaderLogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG,
								"Path " + file.getAbsolutePath() + "does not exist...");
					}

				}

				// Adding jar files from the directories to the list
				if (!unloadedDirectories.isEmpty()) {
					CognitiveClassLoaderLogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG,
							"Found unloaded directories>>>" + unloadedDirectories.toString());
					urlCache.addAll(formURLUsingPath(unloadedDirectories));
					
					// Forming URL array and sending it to classloader
					finalURLs = new URL[urlCache.size()];
					codecClasses = new URLClassLoader((URL[]) urlCache.toArray(finalURLs), loader);
					urlClass = URLClassLoader.class;

					//Non-invasive : Breaking OOPS here!!! Felt it was necessary.
					try {
						Method method = urlClass.getDeclaredMethod("addURL", new Class[] { URL.class });
						method.setAccessible(true);
						for (URL url : codecClasses.getURLs()) {
							method.invoke(loader, new Object[] { url });
						}
					} finally{
						codecClasses.close();
					}

					//Adding the newly loaded jars to java.class.path
					addToJavaClassPath(codecClasses);
					System.setProperty("java.class.path", javaClassPathProperty);
				}
			}
		}
		
		return loader;

	}

	private void addToJavaClassPath(URLClassLoader urlLoader) {

		for (URL url : urlLoader.getURLs()) {
			javaClassPathProperty = javaClassPathProperty + url.getPath() + ":";
		}
	}

	/*
	 * Expands the files in the directories
	 * 
	 */
	private List<URL> formURLUsingPath(List<File> directories) throws MalformedURLException {
		List<URL> urls = new ArrayList<URL>();
		for (File filePath : directories) {

			File[] jarFiles = filePath.listFiles(new FileFilter() {

				public boolean accept(File file) {
					// TODO Auto-generated method stub
					return file.getName().endsWith(CoreConstants.ENDS_WITH_JAR);
				}
			});

			for (int i = 0; i < jarFiles.length; i++) {
				URL url = jarFiles[i].toURI().toURL();
				urls.add(url);
			}
		}
		return urls;
	}

	/*
	 * Parses classpath string
	 * 
	 */
	private List<String> parseClasspath(String classPath) {

		List<String> pathList = new ArrayList<String>();
		String[] paths = classPath.trim().split(":");

		for (int i = 0; i < paths.length; i++) {
			String path = paths[i].trim().replaceAll("/+", "/");
			int pathLength = path.length();

			if (path.contains(CoreConstants.ENDS_WITH_JAR)) {
				if (path.charAt(pathLength - 1) == CoreConstants.BACKSLASH) {
					path = path.substring(0, pathLength - 1);
				}
			} else {
				if (path.charAt(pathLength - 1) == CoreConstants.ASTERISK) {
					path = path.substring(0, pathLength - 1);
				}

				if (path.charAt(path.length() - 1) != CoreConstants.BACKSLASH) {
					path = path + CoreConstants.BACKSLASH;
				}
			}

			paths[i] = path;

		}

		pathList = Arrays.asList(paths);
		CognitiveClassLoaderLogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG,
				"Pathlist>>>" + pathList.toString());
		return pathList;

	}

}
