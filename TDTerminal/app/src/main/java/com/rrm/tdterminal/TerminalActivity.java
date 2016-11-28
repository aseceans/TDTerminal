package com.rrm.tdterminal;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
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
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.KeyListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class TerminalActivity extends AppCompatActivity {
    String VendorName = "Fanshawe College";
    NfcAdapter nfc;
    EditText myET;
    TextView myOutput;
    List<String> terminalOutput;

    InputFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pubnubTerminalService.currentActivity = this;
        setContentView(R.layout.activity_terminal);
        myET = (EditText)findViewById(R.id.priceEdit);
        myOutput = (TextView)findViewById(R.id.textTagContent);


        pubnubTerminalService.PubnubConnect();
        nfc = NfcAdapter.getDefaultAdapter(this);
        if(nfc != null && nfc.isEnabled()){
            Toast.makeText(this, "NFC available!", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this, "NFC NOT available! Please Enable NFC", Toast.LENGTH_LONG).show();

        terminalOutput = new ArrayList<String>();
        filter = new InputFilter() {
            final int maxDigitsBeforeDecimalPoint=4;
            final int maxDigitsAfterDecimalPoint=2;
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                StringBuilder builder = new StringBuilder(dest);
                builder.replace(dstart, dend, source.subSequence(start, end).toString());
                if (!builder.toString().matches("(([1-9]{1})([0-9]{0,"+(maxDigitsBeforeDecimalPoint-1)+"})?)?(\\.[0-9]{0,"+maxDigitsAfterDecimalPoint+"})?"))
                {
                    if(source.length()==0)
                        return dest.subSequence(dstart, dend);
                    return "";
                }
                return null;
            }
        };
        myET.setFilters(new InputFilter[] { filter });
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
            if(myET.getText() == null || myET.getText().toString().equals("") ) {
                Toast.makeText(this, "Please Enter A Price\nBefore Tapping Card", Toast.LENGTH_LONG).show();
            } else {
                double myD = Double.parseDouble(myET.getText().toString());
                ARMessageRequest myARMR = new ARMessageRequest(VendorName, payloadString, myD);
                pubnubTerminalService.SendMessage(pubnubTerminalService.REQUEST, myARMR);
            }
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
            printMessage(connectString);
        }
    };

    private void printMessage(String msg){
        myOutput.setText("");
        if(terminalOutput.size() < 6)
            terminalOutput.add(msg);
        else {
            terminalOutput.remove(0); //remove top index
            terminalOutput.add(msg);
        }
        for (String temp : terminalOutput) {
            myOutput.setText(myOutput.getText() + temp + "\n");
        }
    }

    private Handler messageHandle = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            Bundle data = msg.getData();
            String mydata = data.getString(pubnubTerminalService.RESPONSE);
            printMessage(mydata);
        }
    };

    public Handler getRoomCreatedHandle() {return roomCreatedHandle;}
    public Handler getMessageHandle() {return messageHandle;}

    public void OnNumberPadClick(View view) {
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.showSoftInput(myET, 0);
    }
}
