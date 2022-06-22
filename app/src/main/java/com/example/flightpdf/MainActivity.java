package com.example.flightpdf;

import static androidx.core.content.FileProvider.getUriForFile;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDDocumentCatalog;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDAcroForm;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDTextField;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    File root;
    AssetManager assetManager;
    TextView tv;
    EditText editText, editText2, editText3;
    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setup();
    }

    private void setup() {
        PDFBoxResourceLoader.init(getApplicationContext());
        //Finds the root of the external storage.
        root = android.os.Environment.getExternalStorageDirectory();
        assetManager = getAssets();
        tv = findViewById(R.id.statusTextView);

        //Watchers for button enabling
        button = findViewById(R.id.button);
        editText = findViewById(R.id.editTextAircraftIdentification);
        editText2 = findViewById(R.id.editTextPersonName);
        editText3 = findViewById(R.id.editTextSchedule);
        editText.addTextChangedListener(loginTextWatcher);
        editText2.addTextChangedListener(loginTextWatcher);
        editText3.addTextChangedListener(loginTextWatcher);
    }

    private TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String aircraftID = editText.getText().toString().trim();
            String personName = editText2.getText().toString().trim();
            String schedule = editText3.getText().toString().trim();

            button.setEnabled(!aircraftID.isEmpty() && !personName.isEmpty() && !schedule.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void editPDF(View v){
        EditText editText = findViewById(R.id.editTextAircraftIdentification);
        String aircraftId = editText.getText().toString();
        EditText editText2 = findViewById(R.id.editTextPersonName);
        String editTextPersonName = editText.getText().toString();
        EditText editText3 = findViewById(R.id.editTextSchedule);
        String schedule = editText3.getText().toString();

        try {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            String path = root.getAbsolutePath() + "/Download/"+Long.toString(currentTime)+".pdf";
            PDDocument document = PDDocument.load(assetManager.open("kuke.pdf"));
            document.save(path);

            // Load the document and get the AcroForm
            PDDocumentCatalog docCatalog = document.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();

            // Matricula del avion
            char[] aircraftIdList = aircraftId.toCharArray();
            byte x = 1;
            for (int i = 0; i <= aircraftIdList.length-1; i++) {
                PDTextField field = (PDTextField) acroForm.getField("ID"+x);//ID1-ID7
                field.setValue(Character.toString(aircraftIdList[i]));
                x++;
            }
            // Segundo campo
            PDTextField fields = (PDTextField) acroForm.getField("Markings");
            if(aircraftId.equals("LVCGD"))
                fields.setValue("BLANCO Y VERDE");
            else
                fields.setValue("BLANCO Y AZUL");


            // Horario
            char[] scheduleList = schedule.toCharArray();
            x = 1;
            for (int i = 0; i<4; i++){
                PDTextField field = (PDTextField) acroForm.getField("T"+x);//T1-T4
                field.setValue(Character.toString(scheduleList[i]));
                x++;
            }

            document.save(path);
            document.close();
            tv.setText("Successfully wrote PDF to " + path);
            //asd
            File file = new File(path);
            composeEmail(file);


        } catch (IOException e) {
            tv.setText(e.toString());
        }
    }

    public void composeEmail(File file) {
        Uri Uri = getUriForFile(this,getPackageName()+".provider",file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"pbogert@protonmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "test123");
        intent.putExtra(Intent.EXTRA_TEXT, "message");
        intent.putExtra(Intent.EXTRA_STREAM, Uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}