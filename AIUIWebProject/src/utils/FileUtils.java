package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

	public static void saveBytesToFile(byte[] data) {
		FileOutputStream fop = null;
		File file;
		try {
			file = new File("/home/data");
			fop = new FileOutputStream(file);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			if (data.length > 0) {
				System.out.print("get" + data.length);
				fop.write(data);
			}

			fop.flush();
			fop.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fop != null) {
					fop.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
