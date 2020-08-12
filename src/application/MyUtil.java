package application;

import java.io.File;
import java.net.URISyntaxException;

public class MyUtil {
	/**
	 * 実行するjarのフォルダを得る
	 * @return
	 */
	public static String getJarFolder() {
		File jarFile = null;
		try {
			jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return jarFile.getParent() + File.separator;
	}


}
