package net.did2memo.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;

public class RemoteExecConsole extends IOConsole {

	public RemoteExecConsole(ImageDescriptor imadeDescripter, IProcess process) {
		super("Remote Exec", imadeDescripter);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				IOConsoleInputStream in = RemoteExecConsole.this.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line;;
				try {
					while((line = reader.readLine()) != null) {
						RemoteExecConsole.this.out.write((line).getBytes());
						RemoteExecConsole.this.out.flush();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	private OutputStream out = null;
	public void setRemoteExecOutputStream(OutputStream out) {
		this.out = out;
	}
}
