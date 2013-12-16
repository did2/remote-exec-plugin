package net.did2memo.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

public class RemoteExecConsole extends IOConsole {
	private final Display display;

	private final Color commandColor;
	private final Color commandOutputStdColor;
	private final Color commandOutputErrColor;

	private final IOConsoleOutputStream commandOutputStream;
	private final IOConsoleOutputStream commandOutputStdStream;
	private final IOConsoleOutputStream commandOutputErrStream;

	public RemoteExecConsole(ImageDescriptor imageDescriptor, String encoding) {
		super("Remote", null, imageDescriptor, encoding, false);

		this.display = Display.getDefault();
		this.commandColor = new Color(this.display, new RGB(200, 140, 7));
		this.commandOutputStdColor = new Color(this.display, new RGB(76, 121, 37));
		this.commandOutputErrColor = new Color(this.display, new RGB(255, 70, 7));

		this.commandOutputStream = this.newOutputStream();
		this.commandOutputStdStream = this.newOutputStream();
		this.commandOutputErrStream = this.newOutputStream();

		this.commandOutputStream.setColor(this.commandColor);
		this.commandOutputStdStream.setColor(this.commandOutputStdColor);
		this.commandOutputErrStream.setColor(this.commandOutputErrColor);

		new Thread(new Runnable() {
			@Override
			public void run() {
				IOConsoleInputStream in = RemoteExecConsole.this.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line;
				try {
					while ((line = reader.readLine()) != null) {
						RemoteExecConsole.this.out.write((line).getBytes());
						RemoteExecConsole.this.out.flush();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private OutputStream out = null;

	public void setRemoteExecOutputStream(OutputStream out) {
		this.out = out;
	}

	public IOConsoleOutputStream getConsoleOutputStream(int consoleOutputType) {
		IOConsoleOutputStream out = this.newOutputStream();
		switch (consoleOutputType) {
		case ConsoleOutputType.COMMAND:
			return this.commandOutputStream;
		case ConsoleOutputType.COMMAND_STD_OUT:
			return this.commandOutputStdStream;
		case ConsoleOutputType.COMMAND_ERR_OUT:
			return this.commandOutputErrStream;
		}
		return this.newOutputStream();
	}
}
