package fun.ksnb.hllb.velocity.main;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;

public class VelocityFixLuobo {
    private MethodHandles.Lookup super_lookup;

    @Inject
    public VelocityFixLuobo(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        File dataFile = dataDirectory.toFile();
        if (!dataFile.exists()) {
            dataFile.mkdirs();
        }
        String version = "8.0.25";
        String jarName = "mysql-connector-java-" + version + ".jar";
        try {
            if (getClass("com.mysql.cj.jdbc.Driver") != null) return;
            downloadFile("https://maven.aliyun.com/repository/public/mysql/mysql-connector-java/" + version + "/mysql-connector-java-" + version + ".jar", new File(dataFile, jarName));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("MySql 驱动 贺兰大萝卜库 下载失败");
        }
//        反射入
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("MySql 驱动 贺兰大萝卜库 反射加载失败");
        }
        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            MethodHandle handle = super_lookup.unreflect(getMethodWithParent(classLoader.getClass(), "addURL", false, URL.class));
            handle.invoke(classLoader, new File(dataFile, jarName).toURI().toURL());
        } catch (Throwable e) {
            e.printStackTrace();
            logger.error("MySql 驱动 贺兰大萝卜库 反射加载失败");
        }
        if (getClass("com.mysql.cj.jdbc.Driver") == null) {
            logger.error("MySql 驱动 贺兰大萝卜库 反射加载失败");
        } else {
            logger.info("MySql 驱动 贺兰大萝卜库 加载成功");
        }
    }

    public static Method getMethodWithParent(Class<?> clazz, String name, boolean handleAccessible, Class<?>... args) throws NoSuchMethodException {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.getName().equalsIgnoreCase(name)) continue;
            if (!Arrays.equals(method.getParameterTypes(), args)) continue;
            if (handleAccessible) method.setAccessible(true);
            return method;
        }
        if (clazz != Object.class)
            return getMethodWithParent(clazz.getSuperclass(), name, handleAccessible, args);
        throw new NoSuchMethodException(name + " method in " + clazz.getName());
    }

    private Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            return null;
        }
    }

    private void init() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafeField = getField(unsafeClass, unsafeClass, true);
        Method theUnsafeGetObjectMethod = getMethod(unsafeClass, "getObject", false, Object.class, long.class);
        Method theUnsafeStaticFieldOffsetMethod = getMethod(unsafeClass, "staticFieldOffset", false, Field.class);
        Object theUnsafe = theUnsafeField.get(null);
        Field implLookup = getField(MethodHandles.Lookup.class, "IMPL_LOOKUP", false);

        super_lookup = (MethodHandles.Lookup) theUnsafeGetObjectMethod.invoke(theUnsafe, MethodHandles.Lookup.class, theUnsafeStaticFieldOffsetMethod.invoke(theUnsafe, implLookup));
    }

    public static Field getField(Class<?> clazz, String target, boolean handleAccessible) throws NoSuchFieldException {
        try {
            Field field;
            field = clazz.getDeclaredField(target);
            if (handleAccessible) field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldException(target + " field in " + clazz.getName());
        }
    }

    public static Field getField(Class<?> clazz, Class<?> target, boolean handleAccessible) throws NoSuchFieldException {
        return getField0(clazz, clazz, target, handleAccessible);
    }

    private static Field getField0(Class<?> source, Class<?> clazz, Class<?> target, boolean handleAccessible) throws NoSuchFieldException {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() != target) continue;
            if (handleAccessible) field.setAccessible(true);
            return field;
        }
        clazz = clazz.getSuperclass();
        if (clazz != null) return getField(clazz, target, handleAccessible);
        throw new NoSuchFieldException(target.getName() + " type in " + source.getName());
    }

    public static Method getMethod(Class<?> clazz, String name, boolean handleAccessible, Class<?>... args) throws NoSuchMethodException {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.getName().equalsIgnoreCase(name)) continue;
            if (!Arrays.equals(method.getParameterTypes(), args)) continue;
            if (handleAccessible) method.setAccessible(true);
            return method;
        }
        throw new NoSuchMethodException(name + " method in " + clazz.getName());
    }

    private void downloadFile(String url, File out) throws IOException {
        if (out.exists()) out.delete();
        File downloadingFile = new File(out.getParent(), out.getName() + ".downloading");
        if (downloadingFile.exists()) {
            downloadingFile.delete();
        }
        downloadingFile.createNewFile();

        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(false);
        httpURLConnection.connect();

        int repCode = httpURLConnection.getResponseCode();

        if (repCode == 200) {
            try (InputStream inputStream = httpURLConnection.getInputStream();
                 FileOutputStream fileOutputStream = new FileOutputStream(downloadingFile)) {
                byte[] b = new byte[1024];
                int n;
                while ((n = inputStream.read(b)) != -1) {
                    fileOutputStream.write(b, 0, n);// 写入数据
                }
                fileOutputStream.flush();
            }
            downloadingFile.renameTo(out);
        }
    }
}
