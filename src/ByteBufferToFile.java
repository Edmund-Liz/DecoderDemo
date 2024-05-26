import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ByteBufferToFile {

    public static void main(String[] args) {
        // 假设你已经有一个包含 PNG 数据的 ByteBuffer
        ByteBuffer byteBuffer = ByteBuffer.wrap(getSamplePngData());

        // 目标文件路径
        String filePath = "path/to/your/output/image.png";

        // 将 ByteBuffer 写入文件
        try {
            writeFileFromByteBuffer(byteBuffer, filePath);
            System.out.println("文件写入成功！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeFileFromByteBuffer(ByteBuffer byteBuffer, String filePath) throws IOException {
        // 创建文件对象
        File file = new File(filePath);

        // 如果文件不存在，创建文件
        if (!file.exists()) {
            file.getParentFile().mkdirs(); // 创建父目录
            file.createNewFile();
        }

        // 使用 FileOutputStream 和 FileChannel 将 ByteBuffer 写入文件
        try (FileOutputStream fos = new FileOutputStream(file);
             FileChannel fileChannel = fos.getChannel()) {
            // 确保 ByteBuffer 的位置设定在开始
            byteBuffer.position(0);
            fileChannel.write(byteBuffer);
        }
    }

    // 一个示例方法，返回一些 PNG 数据的字节数组
    private static byte[] getSamplePngData() {
        // 在实际使用时，你会从其他地方获取 PNG 数据
        // 这里返回一个空数组作为示例
        return new byte[0];
    }
}
