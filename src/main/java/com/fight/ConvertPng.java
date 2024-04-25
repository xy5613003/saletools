package com.fight;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

@Log4j2
public class ConvertPng {
    public static void convertPngToJpg(String pngFilePath, String jpgFilePath) {

        try {
            if (!StringUtils.endsWithIgnoreCase(pngFilePath, ".png")) {
                return;
            }
            // 1. 读取PNG图像文件
            BufferedImage pngImage = ImageIO.read(new File(pngFilePath));

            // 2. 创建新的BufferedImage对象
            BufferedImage jpgImage = new BufferedImage(pngImage.getWidth(), pngImage.getHeight(), BufferedImage.TYPE_INT_RGB);

            // 3. 绘制PNG图像到新的BufferedImage对象
            Graphics2D graphics = jpgImage.createGraphics();
            graphics.setBackground(Color.WHITE);
            graphics.fillRect(0, 0, pngImage.getWidth(), pngImage.getHeight());
            graphics.drawImage(pngImage, 0, 0, null);

            // 4. 创建File对象用于保存JPG图像文件
            File jpgFile = new File(jpgFilePath);

            // 5. 将新的BufferedImage对象写入JPG图像文件
            ImageIO.write(jpgImage, "jpg", jpgFile);
        } catch (IOException e) {
            log.error("文件不存在", e);
        }
    }


    public static void watchFileStart(String dir) {
        if (!Files.exists(Paths.get(dir))) {
            return;
        }
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path path = Paths.get(dir);
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            while (true) {
                WatchKey key = watchService.take();
                TimeUnit.SECONDS.sleep(2);
                key.pollEvents().forEach(poll -> {
                    Path subpath = (Path) poll.context();
                    Path resolve = path.resolve(subpath);
                    convertPngToJpg(resolve.toString(), path.resolve("temp").resolve(subpath.toString().replaceAll("(.png|.PNG)", ".jpg")).toString());
                });
                key.reset();
            }

        } catch (IOException e) {
            log.error("watch error", e);
        } catch (InterruptedException e) {
            log.error("InterruptedException error", e);
        }
    }
}
