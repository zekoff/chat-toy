package net.zekoff.android.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChatClient extends Activity implements OnClickListener {
	EditText hostText = null;
	EditText portText = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		TextView hostLabel = new TextView(this);
		hostLabel.setTextSize(24);
		hostLabel.setText("Host address:");
		layout.addView(hostLabel);

		hostText = new EditText(this);
		hostText.setText("192.168.0.0");
		layout.addView(hostText);

		TextView portLabel = new TextView(this);
		portLabel.setTextSize(24);
		portLabel.setText("Port number:");
		layout.addView(portLabel);

		portText = new EditText(this);
		portText.setText("50007");
		layout.addView(portText);

		Button b = new Button(this);
		b.setOnClickListener(this);
		b.setText("CONNECT");
		layout.addView(b);

		setContentView(layout);
	}

	@Override
	public void onClick(View v) {
		String host = hostText.getText().toString();
		String port = portText.getText().toString();
		Intent intent = new Intent(this, ChatScreen.class);
		Bundle extras = new Bundle();
		extras.putString("HOST", host);
		extras.putString("PORT", port);
		intent.putExtras(extras);
		startActivity(intent);
		finish();
	}

}