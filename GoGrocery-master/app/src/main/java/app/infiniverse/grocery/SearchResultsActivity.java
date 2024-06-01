package app.infiniverse.grocery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SearchResultsActivity extends AppCompatActivity implements AddorRemoveCallbacks{

    private ProgressBar mProgressBar;

    LinearLayout ll ;
    ConstraintLayout cl;
    public static final String PREFS = "PREFS";
    SharedPreferences sp;
    int cart_count;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_search_results);
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.mytoolbar);
        sp = getApplicationContext().getSharedPreferences(PREFS, MODE_PRIVATE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProgressBar = findViewById(R.id.progressBar);
        getSupportActionBar().setTitle("Search Results");

        ll= findViewById(R.id.ll_products);
        cl= findViewById(R.id.ll_empty);



        Bundle bundle = getIntent().getExtras();
        final String search_text = bundle.getString("search_text");

        mProgressBar.setVisibility(View.VISIBLE);

//        Toast.makeText(this, sub_cat_id, Toast.LENGTH_SHORT).show();

        class Products extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                String searchProductUrl = getResources().getString(R.string.base_url) + "searchProducts/";

                try {
                    URL url = new URL(searchProductUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_Data = URLEncoder.encode("search_text", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");

                    bufferedWriter.write(post_Data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    String result = "", line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }
                    return result;
                } catch (Exception e) {
                    return e.toString();
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchResultsActivity.this);
                builder.setTitle("Received Message");

                try {

                    JSONArray productArray = new JSONArray(s);

                    String[] product_ids = new String[productArray.length()];
                    String[] product_names = new String[productArray.length()];
                    String[] product_descs = new String[productArray.length()];
                    String[] product_imgs = new String[productArray.length()];
                    String[] product_prices = new String[productArray.length()];
                    String[] product_brands = new String[productArray.length()];
                    String[] product_sps = new String[productArray.length()];
                    String[] product_dps = new String[productArray.length()];


                    JSONObject json_data = new JSONObject();
                    for (int i = 0; i < productArray.length(); i++) {
                        json_data = productArray.getJSONObject(i);
                        product_ids[i] = json_data.getString("id");
                        product_names[i] = json_data.getString("name");
                        product_descs[i] = json_data.getString("description");
                        product_imgs[i] = json_data.getString("image");
                        product_prices[i] = json_data.getString("mrp") + " /-";
                        product_brands[i] = json_data.getString("brand");
                        product_sps[i] = "\u20B9" + json_data.getString("selling_price") + " /-";
                        double p_mrp = Double.parseDouble(json_data.getString("mrp"));
                        double p_sp = Double.parseDouble(json_data.getString("selling_price"));
                        double p_dp = (p_mrp - p_sp) / (p_mrp / 100);
                        int p_dp_i = (int) p_dp;
                        product_dps[i] = String.valueOf(p_dp_i);


                    }
                    mProgressBar.setVisibility(View.GONE);
                    if(productArray.length()==0){

                        cl.setVisibility(View.VISIBLE);
                    }else {

                        ll.setVisibility(View.VISIBLE);
                        RecyclerView product_recyclerview = findViewById(R.id.recyclerview_products);
                        product_recyclerview.setNestedScrollingEnabled(false);
                        product_recyclerview.setLayoutManager(new LinearLayoutManager(SearchResultsActivity.this));
                        product_recyclerview.setAdapter(new Recent_Products_Adapter(product_ids, product_names, product_descs, product_imgs, product_prices, product_brands, product_sps, product_dps, SearchResultsActivity.this));
                    }
                } catch (JSONException e) {
                    builder.setCancelable(true);
                    builder.setTitle("No Internet Connection");
//                    builder.setMessage(e.toString());
                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.show();
                }

            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }


        }
        Products products = new Products();
        products.execute(search_text);


    }


    @Override
    public void onAddProduct() {
        cart_count++;
        invalidateOptionsMenu();

    }

    @Override
    public void onRemoveProduct() {
        cart_count--;
        invalidateOptionsMenu();
    }
}
