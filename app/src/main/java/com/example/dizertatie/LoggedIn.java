package com.example.dizertatie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class LoggedIn extends AppCompatActivity {

    Button signout, btnMap;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        connect();

        signout = findViewById(R.id.btn_Signout);
        btnMap = findViewById(R.id.btn_map);

        auth = FirebaseAuth.getInstance();

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();

                // this listener will be called when there is change in firebase user session
                FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user == null) {
                            startActivity(new Intent(LoggedIn.this, LoginActivity.class));
                            finish();
                        }
                    }
                };
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapIntent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(mapIntent);
            }
        });
    }

    public void connect(){

        String clientId = MqttClient.generateClientId();
        final MqttAndroidClient client =
                new MqttAndroidClient(this.getApplicationContext(), "tcp://104.248.22.204",
                        clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        options.setCleanSession(false);
        options.setUserName("iamroot");
        options.setPassword("iamroot".toCharArray());
        try {
            IMqttToken token = client.connect(options);
            //IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d("file", "onSuccess");
                    //publish(client,"payloadd");
                    subscribe(client,"test/button");
                    client.setCallback(new MqttCallback() {
                        //TextView tt = (TextView) findViewById(R.id.tt);
                        @Override
                        public void connectionLost(Throwable cause) {

                        }
                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            Log.d("file", message.toString());

                            if (topic.equals("test/button")) {
                                Intent fullScreenIntent = new Intent(getApplicationContext(), LoggedIn.class);
                                PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                                        fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                                PendingIntent pendingIntent = TaskStackBuilder.create(getApplication())
                                        .addNextIntent(intent)
                                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                                        .setSmallIcon(R.drawable.logo)
                                        .setContentTitle("ElderCare")
                                        .setContentText("Emergency! See elder's location")
                                        .setContentIntent(pendingIntent)
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setFullScreenIntent(fullScreenPendingIntent, true)
                                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                        .setAutoCancel(true)
                                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

                                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.notify(1, builder.build());

                            }

                        }
                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {

                        }
                    });
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("file", "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(MqttAndroidClient client, String payload){
        String topic = "foo/bar";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(MqttAndroidClient client , String topic){
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}