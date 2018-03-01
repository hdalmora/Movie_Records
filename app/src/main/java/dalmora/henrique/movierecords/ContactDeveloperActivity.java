package dalmora.henrique.movierecords;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import dalmora.henrique.movierecords.Util.Util;

public class ContactDeveloperActivity extends AppCompatActivity {
    public static final String LOG_TAG = ContactDeveloperActivity.class.getSimpleName();

    private FloatingActionButton fab_send_email;
    private EditText mEmailMessageEditText;

    TextView mFabricanteTextview,
             mDisposivitoTextView,
             mModeloTextView,
             mVersaoTextView;


    private String mCausaSpinnerSelected = "";
    int mItemSelectedPosition;

    private String mThemeOptions = null;

    BroadcastReceiver mThemeMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");

            if (message.equals("themeChanged")){
                Log.d(LOG_TAG, "Message: Theme Changed");
                ContactDeveloperActivity.this.recreate();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mThemeMessageReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);

        outState.putString("ItemSelecteName", mCausaSpinnerSelected);
        outState.putInt("ItemSelectedPosition", mItemSelectedPosition);
        Log.d(LOG_TAG, "SPINNER" + " onSaveInstanceState ItemName: " + mCausaSpinnerSelected);
        Log.d(LOG_TAG, "onSaveInstanceState" + "CALLED");

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.d(LOG_TAG, "onViewStateRestored " + "Called");

        if (savedInstanceState != null){
            mCausaSpinnerSelected = savedInstanceState.getString("ItemSelecteName");

            Log.d("SPINNER", "onRestoreInstanceState Item Name: " + mCausaSpinnerSelected);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Setar o tema da activity Com base no tema escolhido nas configurações
        mThemeOptions = Util.getPreferredTheme(this);
        Util.setThemePreference(this, mThemeOptions);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_developer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back); // Icone Back Arrow
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Voltar para Main Activity
                goBack();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mThemeMessageReceiver,
                new IntentFilter("ThemeChanged"));

        mFabricanteTextview = (TextView)  findViewById(R.id.contact_dev_fabricante_textview);
        mDisposivitoTextView = (TextView) findViewById(R.id.contact_dev_dispositivo_textview);
        mModeloTextView = (TextView) findViewById(R.id.contact_dev_modelo_textview);
        mVersaoTextView = (TextView) findViewById(R.id.contact_dev_androidversion_textview);
        mEmailMessageEditText = (EditText) findViewById(R.id.contact_dev_mensagem_edittext);

        fab_send_email = (FloatingActionButton) findViewById(R.id.fab_send_email);
        fab_send_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Enviar Intent para app email com as informações dos editText e spinner
                sendEmail();
            }
        });

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String device = Build.DEVICE;
        String androidVersion = android.os.Build.VERSION.RELEASE + " (" + android.os.Build.VERSION.SDK_INT + ")";
        mFabricanteTextview.setText(manufacturer);
        mDisposivitoTextView.setText(device);
        mModeloTextView.setText(model);
        mVersaoTextView.setText(androidVersion);


        // Setup spinner
        Spinner spinner = (Spinner) findViewById(R.id.contact_dev_spinner_causa);
        spinner.setAdapter(new SpinnerAdapter(
                toolbar.getContext(),
                new String[]{
                        "Reportar Erro/Bug",
                        "Requisição de funcionalidade/melhoria",
                        "Outro",
                }));

        //Restaurar o estado do item selecionado no spinner quando a tela é rotacionada
        if (savedInstanceState != null) {
            //posição do item selecionado no spinner
            int position = savedInstanceState.getInt("ItemSelectedPosition");
            Log.d(LOG_TAG, "SPINNER" + "onRestoreInstanceState Item Position: " + position);
            spinner.setSelection(position);


        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Quando o item da lista for selecionado, mostrar seu conteudo no "container view"

                mCausaSpinnerSelected = parent.getItemAtPosition(position).toString();
                Log.d(LOG_TAG, mCausaSpinnerSelected);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);
                //((TextView) parent.getChildAt(position)).setTextColor(Color.BLACK);

                mItemSelectedPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void goBack() {
        super.finish();
        Log.d("APP_INFO", "Back Arrow Clicked");
    }

    private void sendEmail(){
        if (mEmailMessageEditText.getText() != null) {
            if(mEmailMessageEditText.getText().length() > 14){
                String causa = null;
                String message = mEmailMessageEditText.getText().toString();
                if(mCausaSpinnerSelected != null && mCausaSpinnerSelected.length() > 0){
                    causa = mCausaSpinnerSelected;

                    String fabricante = mFabricanteTextview.getText().toString();
                    String dispositivo = mDisposivitoTextView.getText().toString();
                    String modelo = mModeloTextView.getText().toString();
                    String versao = mVersaoTextView.getText().toString();

                    String mensagem_formatada = message + "\n\n -------------------------------- \n" +
                            "Device Information: \n" +
                            "Manufacturer: " + fabricante + "\n" +
                            "Device: " + dispositivo + "\n" +
                            "Model: " + modelo + "\n" +
                            "Android Version: " + versao + "\n";

                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:" + "movierecords_dev@gmail.com"));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, causa);
                    emailIntent.putExtra(Intent.EXTRA_TEXT, mensagem_formatada);

                    try {
                        String enviar_email_usando = getResources().getString(R.string.str_enviar_email_usando);
                        startActivity(Intent.createChooser(emailIntent, enviar_email_usando));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(this, R.string.str_nenhum_cliente_instalado, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(this, R.string.str_selecione_causa_valida, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, R.string.strmin_caracteres, Toast.LENGTH_SHORT).show();
            }
        }


    }

    private static class SpinnerAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {
        private final ThemedSpinnerAdapter.Helper mDropDownHelper;

        public SpinnerAdapter(Context context, String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                //Inflar o drop down usando o LayoutInflater 
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position));

            return view;
        }

        @Override
        public Resources.Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Resources.Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }

}
