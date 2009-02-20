/**
 * This example MIDlet comes from: 
 * http://developers.sun.com/mobility/midp/ttips/screenlock/index.html
 * 
 * It is a simple example that implements Runnable. Three minor changes have been
 * made so the code works with a current public internet time server:
 * 1. a package name was added of 'example' so the code to be instrumented
 * is clearly distinct from the rest of the libraries, etc.
 * 2. a valid server IP address is used.
 * 3. parsing finishes at the * character instead of \n (the result from the
 * server starts with a newline character). 
 */
package example;
import javax.microedition.midlet.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import java.io.*;

public class TimeMIDlet extends MIDlet implements CommandListener, Runnable {

	private static Display display;
	private Form f;
	private boolean isPaused;
	private StringItem si;
	private Command exitCommand = new Command("Exit", Command.EXIT, 1);
	private Command startCommand = new Command("GetTime", Command.ITEM, 1);

	public TimeMIDlet() {

		display = Display.getDisplay(this);
		f = new Form("Time Demo");
		si = new StringItem("Select GetTime to get the "
				+ "current Time!", " ");

		f.append(si);
		f.addCommand(exitCommand);
		f.addCommand(startCommand);
		f.setCommandListener(this);

		display.setCurrent(f);
	} 

	public void startApp() {
		isPaused = false;
	}

	public void pauseApp() {
		isPaused = true;
	}

	public void destroyApp(boolean unconditional) {
	}

	public void commandAction(Command c, Displayable s) {

		if (c == exitCommand) {
			destroyApp(true);
			notifyDestroyed();
		} else if (c == startCommand) {

			Thread t = new Thread(this);

			t.start();
		}
	}

	public void run() {
		getTime();
	}

	public void getTime() {

		SocketConnection sc = null;
		InputStream is = null;
		Form f = new Form("Time Client");
		StringItem si = new StringItem("Time:" , "");

		f.append(si);
		display.setCurrent(f);

		try {
			sc = (SocketConnection)
                                Connector.open("socket://"
					+ "200.50.25.62"
					+ ":13");

			is = sc.openInputStream();

			StringBuffer sb = new StringBuffer();
			int c = 0;

			while (((c = is.read()) != '*') && (c != -1)) {
				sb.append((char) c);
			}

			si.setText(sb.toString());      

		} catch(IOException e) {

			Alert a = new Alert("TimeClient",
					"Cannot connect to server. "
					+ "Ping the server to make "
					+ "sure it is running...",
					null, AlertType.ERROR);

			a.setTimeout(Alert.FOREVER);
			display.setCurrent(a);
		} finally {
			try {
				if(is != null) {
					is.close();
				}

				if(sc != null) {
					sc.close();
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}    
	} 
}