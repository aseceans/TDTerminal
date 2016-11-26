package com.rrm.tdterminal;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TerminalActivity extends AppCompatActivity {
    String VendorName = "Fanshawe College";
    NfcAdapter nfc;
    EditText myET;
    TextView myTV;
    TextView myOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pubnubTerminalService.currentActivity = this;
        setContentView(R.layout.activity_terminal);
        myET = (EditText)findViewById(R.id.priceEdit);
        myTV = (TextView)findViewById(R.id.connectTV);
        myOutput = (TextView)findViewById(R.id.textTagContent);
        pubnubTerminalService.PubnubConnect();
        nfc = NfcAdapter.getDefaultAdapter(this);
        if(nfc != null && nfc.isEnabled()){
            Toast.makeText(this, "NFC available!", Toast.LENGTH_SHORT).show();
        }
        else
            finish();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        Toast.makeText(this, "NFC intent received!!", Toast.LENGTH_SHORT).show();

        Parcelable[] parcelables = intent.getParcelableArrayExtra(nfc.EXTRA_NDEF_MESSAGES);
        if(parcelables != null && parcelables.length > 0)
        {
            readTextFromMessage((NdefMessage) parcelables[0]);
        }else
        {
            Toast.makeText(this, "No Message!!", Toast.LENGTH_SHORT).show();
        }

        super.onNewIntent(intent);
    }

    private void readTextFromMessage(NdefMessage ndef) {
        NdefRecord[] ndefRecords = ndef.getRecords();
        if(ndefRecords != null && ndefRecords.length > 0)
        {
            NdefRecord ndefrecord = ndefRecords[0];
            byte[] payload = ndefrecord.getPayload();
            String payloadString = new String(payload);
            payloadString = payloadString.substring(3);
            double myD = Double.parseDouble(myET.getText().toString());
            ARMessageRequest myARMR = new ARMessageRequest(VendorName, payloadString, myD);
            pubnubTerminalService.SendMessage(pubnubTerminalService.REQUEST, myARMR);
        }else {
            Toast.makeText(this, "No Records!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause()
    {
        nfc.disableForegroundDispatch(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, TerminalActivity.class);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilter = new IntentFilter[]{};
        nfc.enableForegroundDispatch(this, pendingIntent, intentFilter, null);
        super.onResume();
    }

    // Define a handle to access the context on the main thread. This Handler will handle room created messages.
    private Handler roomCreatedHandle = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            String connectString = "Connected to: " + pubnubTerminalService.ROOM_NAME;
            myOutput.setText(connectString);
        }
    };

    private Handler messageHandle = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            Bundle data = msg.getData();
            String mydata = data.getString(pubnubTerminalService.RESPONSE);
            myOutput.setText(myOutput.getText() + mydata + "\n");
        }
    };

    public Handler getRoomCreatedHandle() {return roomCreatedHandle;}
    public Handler getMessageHandle() {return messageHandle;}
}
