package net.zekoff.android.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ChatScreen extends Activity implements OnClickListener {
	EditText inputView = null;
	TextView outputView = null;
	Button sendButton = null;
	PrintWriter writer = null;
	BufferedReader reader = null;
	String HOST = null;
	String PORT = null;
	StringBuffer messages = new StringBuffer();
	Socket socket = null;
	volatile boolean ready = false;
	Thread listener = null;
	Thread connect = null;
	volatile boolean running = true;
	Handler handler = null;

	@Override
	public void onCreate(Bundle sis) {
		super.onCreate(sis);

		handler = new Handler();

		// set HOST and PORT from intent data
		Bundle extras = getIntent().getExtras();
		HOST = extras.getString("HOST");
		PORT = extras.getString("PORT");

		connect = new Thread(new ConnectionThread());
		connect.start();

		listener = new Thread(new ListenerThread());
		listener.start();

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		outputView = new TextView(this);
		outputView.setTextSize(18);
		inputView = new EditText(this);
		sendButton = new Button(this);

		layout.addView(outputView);
		layout.addView(inputView);
		layout.addView(sendButton);

		sendButton.setText("SEND");
		sendButton.setOnClickListener(this);

		setContentView(layout);
	}

	private class ListenerThread implements Runnable {
		@Override
		public void run() {
			while (!ready) {
				try {
					// Just to play nice and not eat up processor time
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				reader = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			while (true && running) {
				try {
					messages.append("SERVER: " + reader.readLine() + "\n");
					handler.post(new Runnable() {

						@Override
						public void run() {
							outputView.setText(messages.toString());
						}

					});
				} catch (IOException e) {
					e.printStackTrace();
					running = false;
				}
			}
		}

	}

	private class ConnectionThread implements Runnable {

		@Override
		public void run() {
			try {
				socket = new Socket(HOST, Integer.parseInt(PORT));
				writer = new PrintWriter(socket.getOutputStream());
				ready = true;
				handler.post(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(ChatScreen.this,
								"Connection established.", 0).show();
					}

				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

	}

	@Override
	public void onClick(View v) {
		if (ready) {
			String input = inputView.getText().toString();
			writer.println(input);
			writer.flush();
			messages.append("CLIENT: " + input + "\n");
			outputView.setText(messages.toString());
			inputView.setText("");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(3, 333, 0, "Disconnect/Restart");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 333:
			startActivity(new Intent(this, ChatClient.class));
			running = false;
			if (listener.isAlive())
				try {
					listener.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			if (connect.isAlive())
				try {
					connect.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			finish();
			return true;
		}
		return false;
	}
}
