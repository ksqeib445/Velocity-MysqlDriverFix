package fun.ksnb.hllb.velocity.main;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import fun.ksnb.hllb.velocity.task.FileDownloadTask;
import fun.ksnb.hllb.velocity.util.ReflectUtil;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

public class VelocityFixLuobo {
    private static final String MYSQL_VERSION = "8.0.30";
    private static final String MYSQL_SHA256 = "b5bf2f0987197c30adf74a9e419b89cda4c257da2d1142871f508416d5f2227a";
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public VelocityFixLuobo(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) throws Exception {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        if (ReflectUtil.getClass("com.mysql.cj.jdbc.Driver") != null) {
            logger.info("服务端已自带 MySQL 依赖，植入终止！");
            return;
        }
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory);
        }
        if (loadCache()) {
            return;
        }
        downloadAndLoad();
    }

    private String getMySQLLibraryName() {
        return "mysql-connector-java-" + MYSQL_VERSION + ".jar";
    }

    private File getMySQLLiraryFile() {
        return new File(dataDirectory.toFile(), getMySQLLibraryName());
    }

    private String getMySQLDownloadUrl() {
        return "https://maven.aliyun.com/repository/public/mysql/mysql-connector-java/"
                .concat(MYSQL_VERSION)
                .concat("/")
                .concat(getMySQLLibraryName());
    }

    public boolean loadCache() throws Exception {
        File libraryFile = getMySQLLiraryFile();
        if (libraryFile.exists()) {
            if (getSha256(libraryFile).equals(MYSQL_SHA256)) {
                logger.info("缓存文件校验成功，正在植入...");
                try {
                    ReflectUtil.addFileLibrary(libraryFile);
                } catch (Throwable e) {
                    logger.error("在尝试植入依赖时出现异常", e);
                }
                logger.info("MySql 驱动 贺兰大萝卜库 植入成功");
                return true;
            }
            logger.warn("缓存的 MySQL 依赖 Sha256 检查不通过，将重新下载！");
        }
        return false;
    }

    public void downloadAndLoad() throws Exception {
        logger.info("正在下载: " + getMySQLLibraryName());
        File libraryFile = getMySQLLiraryFile();
        Path path = new FileDownloadTask(getMySQLDownloadUrl(), libraryFile.toPath()).call();
        if (!getSha256(path.toFile()).equals(MYSQL_SHA256)) {
            throw new RuntimeException("下载的新文件没有通过 Sha256 校验，请排查！");
        }
        logger.info("依赖下载成功，正在植入...");
        try {
            ReflectUtil.addFileLibrary(libraryFile);
        } catch (Throwable e) {
            logger.error("在尝试植入依赖时出现异常", e);
        }
        logger.info("MySql 驱动 贺兰大萝卜库 植入成功");
    }

    // 获得文件sha256
    private String getSha256(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
            byte[] buff = new byte[1024];
            int n;
            while ((n = fis.read(buff)) > 0) {
                baos.write(buff, 0, n);
            }
            final byte[] digest = MessageDigest.getInstance("SHA-256").digest(baos.toByteArray());
            StringBuilder sb = new StringBuilder();
            for (byte aByte : digest) {
                String temp = Integer.toHexString((aByte & 0xFF));
                if (temp.length() == 1) {
                    sb.append("0");
                }
                sb.append(temp);
            }
            return sb.toString();
        }
    }
}
