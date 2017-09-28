package com.example.hackzurich;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.hackzurich.blockchain.BlockChainCollection;
import com.example.hackzurich.blockchain.Principle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add some general information about transaction, like list of few recent transactions, total budget etc. I guess some ListView would be okay for this
 */

public class MainActivity extends AppCompatActivity implements Principle.PrincipleActionHandler {

    private static final String TAG="MainActivity";

    CoordinatorLayout coordinator;
    Principle myPrinciple;

    private Dialog dialog;

    public static BlockChainCollection blockChainCollection=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        coordinator = (CoordinatorLayout) findViewById(R.id.coordinator_main);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.send_input, null))
                .setTitle("Enter amount")
                // Add action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        startCommunicationActivity(CommunicationLayer.SENDER);
                    }
                })
                .setNegativeButton("Cancel", null);
        dialog = builder.create();

        try {
            myPrinciple=new Principle(this);
            myPrinciple.start();

            Log.d(TAG, "Loading blockchain from file");
            FileInputStream fis = openFileInput("blockchains.hck");
            ObjectInputStream is = new ObjectInputStream(fis);
            MainActivity.blockChainCollection=(BlockChainCollection)is.readObject();
            is.close();
            fis.close();

            MainActivity.blockChainCollection.setPrinciple(myPrinciple);

        } catch (Exception e) {
            Log.d(TAG, "Creatin new blockchain collection");
            MainActivity.blockChainCollection=new BlockChainCollection(myPrinciple);
            e.printStackTrace();
        }

        try {
            Log.d(TAG, "Starting to sign text");
            String text="Milan";
            byte[] signature=myPrinciple.signText(text);
            if(myPrinciple.verify(text,signature)){
                Log.d(TAG,"YEEES");
            }else{
                Log.d(TAG, "Noooo!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        refreshList();

        Button rcv = (Button) findViewById(R.id.receive_button);
        rcv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCommunicationActivity(CommunicationLayer.RECEIVER);
            }
        });

        Button send = (Button) findViewById(R.id.send_button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });
    }

    private void refreshList() {
        List<String> strings = new ArrayList<String>();
        for (BlockChainCollection.Balance b : blockChainCollection.getBalanceList()) {
            if (b.amount < 0) {
                strings.add(String.format("%s owes you CHF%.2f", b.name, Math.abs((float) b.amount) / 100));
            } else if (b.amount > 1) {
                strings.add(String.format("You owe %s CHF%.2f", b.name, Math.abs((float) b.amount) / 100));
            } else {
                strings.add(String.format("You and %s are quit", b.name));
            }
        }

        ListView listView = (ListView) findViewById(R.id.transactions);
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, strings));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // see history, change name, etc
            }
        });
    }

    private void startCommunicationActivity(int type) {
        Intent intent = new Intent(MainActivity.this, QrCommunicationActivity.class);
        intent.putExtra(CommunicationLayer.TYPE_MESSAGE, type);
        if (((EditText) dialog.findViewById(R.id.send_value)) != null){
            intent.putExtra("value", ((EditText) dialog.findViewById(R.id.send_value)).getText().toString());
        }
        startActivityForResult(intent, QrCommunicationActivity.EXCHANGE_BLOCKS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Toast.makeText(this, "Transaction successful",
                Toast.LENGTH_LONG).show();

        refreshList();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void initResetPrinciple(){
        Log.d(TAG, "Principle has requested initialization");
        try{
            myPrinciple.generateKeyPair();
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
    }

    private void saveBlockchains(){
        try{
            Log.d(TAG,"Saving blockchains to file");
            FileOutputStream fos=openFileOutput("blockchains.hck", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(MainActivity.blockChainCollection);
            os.close();
            fos.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

}
