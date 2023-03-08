package fun.ksnb.hllb.velocity.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

public class FileDownloadTask implements Callable<Path> {
    private final String downloadUrl;
    private final Path toPath;

    public FileDownloadTask(String downloadUrl, Path toPath) {
        this.downloadUrl = downloadUrl;
        this.toPath = toPath;
    }

    @Override
    public Path call() throws Exception {
        Files.deleteIfExists(toPath);
        File downloadingFile = new File(toPath.toFile().getParentFile(), toPath.toFile().getName() + ".downloading");
        Files.deleteIfExists(downloadingFile.toPath());
        Files.createFile(downloadingFile.toPath());


        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(downloadUrl).openConnection();
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
            Files.move(downloadingFile.toPath(), toPath);
        }
        return toPath;
    }
}
