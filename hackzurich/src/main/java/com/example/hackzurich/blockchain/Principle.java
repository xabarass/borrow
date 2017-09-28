package com.example.hackzurich.blockchain;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;

import javax.security.auth.x500.X500Principal;


public class Principle {
    public interface PrincipleActionHandler {
        public void initResetPrinciple();
        public Context getContext();
    }

    private static final String TAG="Principle";

    private static final String SHARED_PREF_NAME="hackzurich_super_secure_file";
    private  static final String KEY_PRIVATE_KEY="privatekey";
    private  static final String KEY_PUBLIC_KEY="pubkey";

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private PrincipleActionHandler actionHandler;
    SharedPreferences sp;

    public Principle(PrincipleActionHandler pah){
        this.actionHandler=pah;
    }

    public void start() throws Exception{
        Log.d(TAG, "Starting principle");
        sp=actionHandler.getContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        String privateKeyEncoded=sp.getString(KEY_PRIVATE_KEY, null);
        String publicKeyEncoded=sp.getString(KEY_PUBLIC_KEY, null);

        if(privateKeyEncoded==null || publicKeyEncoded==null){
            Log.d(TAG, "There are no keys, we need to generate them");
            actionHandler.initResetPrinciple();
            return;
        }

        KeyFactory kf= KeyFactory.getInstance("RSA", "BC");

        privateKey=kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(privateKeyEncoded, Base64.DEFAULT)));
        publicKey=kf.generatePublic(new X509EncodedKeySpec(Base64.decode(publicKeyEncoded, Base64.DEFAULT)));

        Log.d(TAG,"We have read the private keys and public as well");
    }

    public void generateKeyPair() throws Exception{
        Log.d(TAG, "Generating new keypair");

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
        keyGen.initialize(512, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();

        SharedPreferences.Editor editor = sp.edit();

        editor.putString(KEY_PRIVATE_KEY, Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT));
        editor.putString(KEY_PUBLIC_KEY, Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT));
        editor.commit();

        Log.d(TAG, "Keypair has been generated and saved to shared prefs");
    }

    public byte[] signText(String text) throws SignatureException, InvalidKeyException, Exception {
        Signature signature = Signature.getInstance("SHA1withRSA", "BC");
        signature.initSign(privateKey, new SecureRandom());
        byte[] message = text.getBytes();
        signature.update(message);
        byte[] sigBytes = signature.sign();
        return sigBytes;
    }

    public byte[] signFields(ArrayList<byte[]> fields) throws SignatureException, InvalidKeyException, Exception {
        Signature signature = Signature.getInstance("SHA1withRSA", "BC");
        signature.initSign(privateKey, new SecureRandom());
        for (byte[] field:fields) {
            signature.update(field);
        }
        byte[] sigBytes = signature.sign();
        return sigBytes;
    }

    public boolean verify(String text, byte[] sigBytes) throws Exception{
        Signature signature = Signature.getInstance("SHA1withRSA", "BC");
        signature.initVerify(publicKey);
        signature.update(text.getBytes());

        return signature.verify(sigBytes);
    }

    public PublicKey getPublicKey(){
        return publicKey;
    }
}
