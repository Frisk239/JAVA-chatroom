package Client;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 * 播放音频 
 */
public class PlayWAV {
	private String str;

	public void Play(String s) {
		this.str = s;
		try {
			AudioInputStream ais = AudioSystem
					.getAudioInputStream(new File(str));// 获取音频输入流
			AudioFormat baseFormat = ais.getFormat();// 获取音频流的格式
			// System.out.println("baseFormat=" + baseFormat);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class,
					baseFormat);
			// System.out.println("info=" + info);
			SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
			// 从音频流读取数据
			// System.out.println("line=" + line);
			line.open(baseFormat);// 以指定格式打开行
			line.start();// 允许行执行数据 I/O
			int BUFFER_SIZE = 4000 * 4;
			int intBytes = 0;
			byte[] audioData = new byte[BUFFER_SIZE];
			while (intBytes != -1) {
				intBytes = ais.read(audioData, 0, BUFFER_SIZE);// 从音频流读取字节
				if (intBytes >= 0) {
					int outBytes = line.write(audioData, 0, intBytes);// 通过源数据行写入音频数据
				}
			}

		} catch (Exception e) {
		}
	}

}
