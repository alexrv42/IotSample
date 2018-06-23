package com.picart.iotsample;

import android.support.design.widget.TabLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MainActivity extends AppCompatActivity {
    // para las tabs, no tocar
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;


    // Atributos a modificar
    String topicTemperatura = "sensores/temperatura";
    String topicHumedad = "sensores/humedad";
    String publishTopic = "input";
    String broker = "192.168.1.64";
    String mensajeOn = "ON";
    String mensajeOff = "OFF";


    // atributos avanzados
    MqttAndroidClient mqttAndroidClient;
    MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
    String serverUri = "tcp://"+ broker +":1883";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));




        /*CONEXIÓN AL BROKER*/
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, System.currentTimeMillis() + "");
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {

                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Conected to broker " + serverUri);
                    notificar("Conectado a broker: " + broker);

                    suscribirTemperatura();
                    suscribirHumedad();
                }


                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Failed to connect to: " + serverUri);
                    notificar("Conexión fallida");
                }
            });

        } catch (MqttException ex) {
            ex.printStackTrace();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.control_tab, container, false);
            TextView textView = rootView.findViewById(R.id.temp_val);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
//            return PlaceholderFragment.newInstance(position + 1);


            switch (position) {
                case 0:
                    return new MonitorTab();
                case 1:
                    return new ControlTab();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
    }


    /*---------------------------------------------------------------------------------------*/
    /* MÉTODOS DE MQTT */
    public void suscribirTemperatura() {
        try {

            mqttAndroidClient.subscribe(topicTemperatura, 0, new IMqttMessageListener() {

                public void messageArrived(String topic, MqttMessage message) {
                    // message Arrived!
                    String messageText = new String(message.getPayload());
                    System.out.println("Message: " + topic + " : " + messageText);

                    TextView label = findViewById(R.id.temp_val);

                    String valor = messageText + "° C";
                    label.setText(valor);
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }


    public void suscribirHumedad() {
        try {

            mqttAndroidClient.subscribe(topicHumedad, 0, new IMqttMessageListener() {

                public void messageArrived(String topic, MqttMessage message) {
                    String messageText = new String(message.getPayload());
                    System.out.println("Message: " + topic + " : " + messageText);

                    TextView label = findViewById(R.id.hum_val);

                    String valor = messageText;
                    label.setText(valor);
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }


    public void botonOn(View v) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(mensajeOn.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
            notificar("Mensaje publicado");
            System.out.println("Message Published");
            if (!mqttAndroidClient.isConnected()) {
                System.out.println(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            notificar("Error publicando");
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void botonOff(View v) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(mensajeOff.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
            notificar("Mensaje publicado");
            System.out.println("Message Published");
            if (!mqttAndroidClient.isConnected()) {
                System.out.println(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            notificar("Error publicando");
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void notificar(String text)
    {
        Snackbar.make(findViewById(R.id.main_content), text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
