package pwc.org.myapiai;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.util.Map;

import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity implements AIListener,View.OnClickListener {
    private Button listenButton,btn_text_process;
    private TextView resultTextView;
    private EditText natLangText;
    private AIService aiService;
    private AIDataService aiDataService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listenButton= (Button) findViewById(R.id.btn_process);
        natLangText= (EditText) findViewById(R.id.natLangText);
        btn_text_process= (Button) findViewById(R.id.btn_text_process);
        resultTextView= (TextView) findViewById(R.id.resultTextview);

        listenButton.setOnClickListener(this);
        btn_text_process.setOnClickListener(this);

        final AIConfiguration config = new AIConfiguration(Configuration.CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiDataService=new AIDataService(this,config);

        aiService.setListener(this);

        natLangText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_GO){
                    //hideKeyboard
                    resultTextView.setTextColor(ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_dark));
                    resultTextView.setText("please wait....");
                    new QueryProcessASycTask().execute(natLangText.getText().toString());
                }

                return false;
            }
        });
    }

    public void listenButtonOnClick(final View view) {
        aiService.startListening();
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

    @Override
    public void onResult(ai.api.model.AIResponse response) {
        Result result = response.getResult();

        // Get parameters
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }

        // Show results in TextView.
       /* resultTextView.setText("Query:" + result.getResolvedQuery() +
                "\nAction: " + result.getAction() +
                "\nFullFillment display text: "+result.getFulfillment().getDisplayText()+
                "\nFullFillment speech: "+result.getFulfillment().getSpeech() +
                "\nParameters: " + parameterString);*/
       if(result.getFulfillment().getDisplayText()!=null){
           resultTextView.setTextColor(ContextCompat.getColor(MainActivity.this,android.R.color.holo_blue_light));
           String displayText="";
           String fullfillmentResult[]=result.getFulfillment().getDisplayText().split(",");
           for(int index=0;index<fullfillmentResult.length;index++){
               displayText=displayText+fullfillmentResult[index]+"\n";
           }


           resultTextView.setText(displayText);
       }
       else {
           resultTextView.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.themeColor));
           resultTextView.setText("There was problem while processing request.Please try agin later");
       }

    }

    @Override
    public void onError(ai.api.model.AIError error) {
        resultTextView.setText(error.toString());

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    @Override
    public void onClick(View view) {
       if(view.getId()==R.id.btn_text_process){
           resultTextView.setTextColor(ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_dark));
           resultTextView.setText("please wait....");
new QueryProcessASycTask().execute(natLangText.getText().toString());
       }
       else {
           listenButtonOnClick(view);
       }

    }

    class QueryProcessASycTask extends AsyncTask<String, Void, AIResponse>{

        @Override
        protected AIResponse doInBackground(String... params) {
            final AIRequest aiRequest = new AIRequest();
            aiRequest.setQuery(params[0]);
            try {
                final AIResponse response = aiDataService.request(aiRequest);
                return response;
            } catch (AIServiceException e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPostExecute(AIResponse aiResponse) {
            super.onPostExecute(aiResponse);
            if (aiResponse != null) {
                // process aiResponse here
                onResult(aiResponse);
            }
            else {
                resultTextView.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.themeColor));
                resultTextView.setText("There was problem while processing request.Please try agin later");
            }
        }
    }
}
