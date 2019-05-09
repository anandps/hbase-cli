package com.hbase.cli.loaders;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import com.hbase.cli.schemaobjects.Codec;

/**
 * Dynamically loads the implementation classes for the given interface
 *
 * @param <T>
 */
public final class CodecImplLoader<T> {

	@SuppressWarnings("rawtypes")
	private static CodecImplLoader loaderInstance;

	private Map<String, T> implementationMapper = new HashMap<String, T>();

	private static final NotificationLogger CodecImplLoaderLogger = NotificationLoggerFactory.getLogger();

	private static final String LOGGING_CLASS_NAME = CodecImplLoader.class.getName();

	// Restricting instantiation
	private CodecImplLoader() {
	}

	private static <T> void createInstance() {
		if (loaderInstance == null) {
			loaderInstance = new CodecImplLoader<T>();
		}
	}

	/**
	 * Dynamically Searches for the class given in codec->classname in the
	 * classloader formed from codec->classpath
	 * 
	 * @param tableName
	 * @param interfaceName
	 * @param codec
	 * @return
	 * @throws MalformedURLException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T load(String tableName, Class<T> interfaceName, Codec codec) throws Exception {
		createInstance();
		return (T) loaderInstance.loadImplementation(tableName, interfaceName, codec, null);
	}

	/**
	 * Dynamically Searches for the class given in codec->classname in the given
	 * classloader
	 * 
	 * @param tableName
	 * @param interfaceName
	 * @param codec
	 * @param loader
	 * @return
	 * @throws MalformedURLException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T load(String tableName, Class<T> interfaceName, Codec codec, ClassLoader loader)
			throws Exception {
		createInstance();
		return (T) loaderInstance.loadImplementation(tableName, interfaceName, codec, loader);

	}

	@SuppressWarnings("unchecked")
	private T loadImplementation(String tableName, Class<T> interfaceName, Codec codec, ClassLoader loader)
			throws Exception {

		T instance = null;
		Class<T> service;
		service = interfaceName;
		if (implementationMapper.containsKey(tableName)) {
			instance = implementationMapper.get(tableName);
		} else {

			if (loader == null) {
				loader = CognitiveClassLoader.getClassLoader(codec.getClassPath());
			}

			if (codec.getClassName() != null && !codec.getClassName().isEmpty()) {
				Class<?> c = Class.forName(codec.getClassName(), false, loader);
				service = (Class<T>) c.getClassLoader().loadClass(interfaceName.getName());
				CodecImplLoaderLogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG,
						c.getClassLoader() + ",,," + service.getClassLoader());
				CodecImplLoaderLogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG,
						"Checking the loaded implementation compatibility>>" + service.isAssignableFrom(c));
				instance = service.cast(c.newInstance());

				// save the implementation for the future retrieval
				implementationMapper.put(tableName, instance);
			}

		}
		CodecImplLoaderLogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG,
				"Entries in loader>>>" + implementationMapper.toString());
		return instance;
	}

}
