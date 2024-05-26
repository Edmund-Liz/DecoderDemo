import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultipartDecoder {



    private static int indexOf(ByteBuffer buffer, ByteBuffer subArray, int start) {
        outer: for (int i = start; i <= buffer.limit() - subArray.limit(); i++) {
            for (int j = 0; j < subArray.limit(); j++) {
                if (buffer.get(i + j) != subArray.get(j)) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
//以start为起始位置，查找下一个Boundary的位置。
//当未找到下一个boundary时判断是已经读取到byteBuffer结尾且有“--”标识还是BadRequest


    private static String extractHeaderValue(String headers, String headerName) {
        String searchString = headerName + "=\"";
        int startIndex = headers.indexOf(searchString);
        if (startIndex == -1) {
            return null;
        }
        startIndex += searchString.length();
        int endIndex = headers.indexOf("\"", startIndex);
        if (endIndex == -1) {
            return null;
        }
        return headers.substring(startIndex, endIndex);
    }

    static FileItem sliceByteBuffer(ByteBuffer byteBuffer, int start, int end) {
        // 找到Headers和内容的分隔位置
        String doubleCRLF = "\r\n\r\n";
        byte[] doubleCRLFBytes = doubleCRLF.getBytes(StandardCharsets.UTF_8);
        ByteBuffer sonByte=ByteBuffer.allocate(doubleCRLFBytes.length);
        sonByte.put(doubleCRLFBytes);


        int headerEnd = indexOf(byteBuffer, sonByte, start);
        if (headerEnd == -1 || headerEnd + doubleCRLFBytes.length >= end) {
            throw new IllegalArgumentException("Malformed multipart request: no headers found");
        }

        // 提取Headers
        byte[] headerBytes = new byte[headerEnd - start];
        byteBuffer.position(start);
        byteBuffer.get(headerBytes, 0, headerBytes.length);
        String headers = new String(headerBytes, StandardCharsets.UTF_8);

        // 提取内容
        int contentStart = headerEnd + doubleCRLFBytes.length;
        int contentLength = end - contentStart;
        ByteBuffer contentBuffer = byteBuffer.duplicate();
        contentBuffer.position(contentStart);
        contentBuffer.limit(contentStart + contentLength);

        // 从Headers中提取key和确定类型
        String key = extractHeaderValue(headers, "name");
        String filename = extractHeaderValue(headers, "filename");
        Type type = (filename != null) ? Type.FILE : Type.VALUE;
        String value;

        if (type == Type.FILE) {
            byte[] contentBytes = new byte[contentLength];
            contentBuffer.get(contentBytes);
            value = new String(contentBytes, StandardCharsets.ISO_8859_1); // 保持二进制数据不变
            key=filename;
        } else {
            byte[] contentBytes = new byte[contentLength];
            contentBuffer.get(contentBytes);
            value = new String(contentBytes, StandardCharsets.UTF_8);
        }

        return new FileItem(key, value, type);
    }


    static List<FileItem> decode(ByteBuffer byteBuffer){

        String string="------WebKitFormBoundary7MA4YWxkTrZu0gW";
        byte[] byteArray = string.getBytes(StandardCharsets.UTF_8);
        ByteBuffer boundary=ByteBuffer.allocate(byteArray.length);
        boundary.put(byteArray);


        int limit=byteBuffer.limit();
        int boundaryLength=boundary.position();
        int start=boundaryLength;
        List<FileItem> parts=new ArrayList<>();
        int i=0;

        while(start<limit)
        {
            int index=indexOf(byteBuffer,boundary,start);
            if(index<0)//正常结束则返回-1，“--”标识已在IndexOf中确认，不再读取
            {
                //利用parts将数据写入request，Finsh
                return parts;
            }else{
                parts.add(sliceByteBuffer(byteBuffer,start,index));
                start=index+boundaryLength;
            }
        }
        return  null;
    }
//调用IndexOf查找下一个Boundary位置，获取part并构造FileItem数组，最后将数据写入request。

    public static void main(String[] args) throws IOException {
        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        MultipartFormDataBuilder builder = new MultipartFormDataBuilder(boundary);


        // Add a form field
        builder.addFormField("username", "test_user");

        // Add a file part
        String fileName = "test.txt";
        byte[] fileContent = "This is a test file.".getBytes(StandardCharsets.UTF_8);
        builder.addFilePart("file", fileName, fileContent, "text/plain");

        // Build the request body
        ByteBuffer requestBody = builder.build();

        List<FileItem> parts=decode(requestBody);

        for (FileItem e:parts){

            System.out.println(e.key+":"+e.value);

        }


    }


}
