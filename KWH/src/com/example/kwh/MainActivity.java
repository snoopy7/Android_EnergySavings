package com.example.kwh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnClickListener, OnItemSelectedListener, OnItemClickListener{
	
	
	JSONArray my_data = null;
	boolean gotData = false;
	boolean newapi = false;
	Spinner states;
	Button search, graph;
	String stateFinal;
	String KWHurl, naturalGasUrl, gasolineUrl;
	GridView gridview;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initialize();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	private void initialize(){
	
		states = (Spinner) findViewById(R.id.spState);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.states_arrays, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		states.setAdapter(adapter);
		
		gridview = (GridView) findViewById(R.id.gridView1);
		gridview.setAdapter(new ImageAdapter(this));
		gridview.setOnItemClickListener(this);
		gridview.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
		
		if(newapi == true){
			gridview.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
			gridview.setMultiChoiceModeListener(new MultiChoiceModeListener());
		}
		
		search = (Button) findViewById(R.id.btSearch);
		graph = (Button) findViewById(R.id.btGraph);
		search.setOnClickListener(this);
		graph.setOnClickListener(this);
			
		
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		if(v.equals(graph)){
			
			if(gotData){
				int index = 0;
				double years[];
				double cost[];
			
				
				
				//data is an array. find how long the array is
				// and log it for debugging 
				while ( my_data.isNull(index) != true ){
					
					index++;
				}
				Log.v("LENGTH", String.valueOf(index) );
				
				
				
				years = new double[index];
				cost = new double[index];
				
				for( int i = 0 ; i < index ; i++) {
					
					try {
						years[i] = Double.valueOf(my_data.getJSONArray(i).getString(0));
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						cost[i] = Double.valueOf(my_data.getJSONArray(i).getString(1));
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
			
			
				
				//debugging individual cost and 
				//year double arrays
				for( int k = 0; k < index ; k++){
					Log.v("YEARS ARRAY", String.valueOf(years[k]));
				
				}
				
				for( int l = 0; l < index ; l++){
					Log.v("COST ARRAY", String.valueOf(cost[l]));
				
				}
				
		
				
				Graph lineGraph = new Graph();
				Intent lineIntent = lineGraph.getIntent(this, cost, years, index );
				
				startActivity(lineIntent);
			}
				
			
		}
		else{
			
			
			if(stateFinal != null ){ 
				KWHurl = getKWHUrl();
				naturalGasUrl = getNaturalGasUrl();
				gasolineUrl = getGasolineUrl();
				
				new AccessAPIKWH().execute(KWHurl);
				
			}
		}	
	}
	
	
	public String getKWHUrl(){
		
		//annual kwh data url
		return "http://api.eia.gov/series/?api_key=3FBC89E53B8E3CB592BA2BB733C07050&series_id=ELEC.PRICE."
		+ stateFinal + "-RES.A";
		
	}
	public String getNaturalGasUrl(){
		
		//annual natural gas data url
		
		return "http://api.eia.gov/series?api_key=YOUR_API_KEY_HERE&series_id=NG.N3010" + stateFinal + "3.A";

	}
	public String getGasolineUrl(){
		
		//weekly gasoline data urls
		if(stateFinal == "CA" || stateFinal == "CO" || stateFinal == "FL" || 
		   stateFinal == "MA" || stateFinal == "MN" || stateFinal == "NY" || 
		   stateFinal == "OH" || stateFinal == "TX" || stateFinal == "WA" ){
			
			
			return "http://api.eia.gov/series?api_key=YOUR_API_KEY_HERE&series_id=PET.EMM_EPMRU_PTE_S" +stateFinal + "_DPG.W";
			
		
		//new england padd
		}else if (stateFinal == "CN" || stateFinal == "ME" || stateFinal == "NH"
			    || stateFinal == "RI" || stateFinal == "VT" ){
				
			return  "http://api.eia.gov/series?api_key=YOUR_API_KEY_HERE&series_id=PET.EMM_EPMRU_PTE_R1X_DPG.W" ;
		
		// central atlantic padd
		}else if(stateFinal == "DE" || stateFinal == "MD" || stateFinal == "NJ" ||
				stateFinal == "PA"){
			
			return "http://api.eia.gov/series?api_key=YOUR_API_KEY_HERE&series_id=PET.EMM_EPMRU_PTE_R1Y_DPG.W";
				
		//lower atlantic padd	
		}else if(stateFinal == "GA" || stateFinal == "NC" || stateFinal == "SC" ||
				stateFinal == "VA" || stateFinal == "WV" ){
			
			return "http://api.eia.gov/series?api_key=YOUR_API_KEY_HERE&series_id=PET.EMM_EPMRU_PTE_R1Z_DPG.W";
		
		//west coast padd
		}else if(stateFinal == "AK" || stateFinal == "AZ" || stateFinal == "HI" ||
				stateFinal == "NV" || stateFinal == "OR"){
			
			return "http://api.eia.gov/series?api_key=YOUR_API_KEY_HERE&series_id=PET.EMM_EPMRU_PTE_R5XCA_DPG.W";

			
		//gulf coast padd	
		}else if(stateFinal == "AL" || stateFinal == "AR" || stateFinal == "LA" ||
				stateFinal == "MS" || stateFinal == "NM"){
			
			return "http://api.eia.gov/series?api_key=YOUR_API_KEY_HERE&series_id=PET.EMM_EPMRU_PTE_R30_DPG.W";
		
		//rocky mountain padd
		}else if(stateFinal == "ID" || stateFinal == "MT" || stateFinal == "UT" ||
				stateFinal == "WY"){
			
			
			return "http://api.eia.gov/series?api_key=YOUR_API_KEY_HERE&series_id=PET.EMM_EPMRU_PTE_R40_DPG.W";
		
		// midwest padd
		}else {
			
			return "http://api.eia.gov/series?api_key=YOUR_API_KEY_HERE&series_id=PET.EMM_EPMRU_PTE_R20_DPG.W";

		}
	}
	
	

	private class AccessAPIKWH extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... url) {
			// TODO Auto-generated method stub
			
	
			String kwh_s = null;
			HttpResponse response = null;
			DefaultHttpClient myClient_1 = new DefaultHttpClient();
			HttpContext localContext_1 = new BasicHttpContext();
			HttpPost httppost = new HttpPost(url[0]);
		
		
			try {
				response = myClient_1.execute(httppost, localContext_1);
				InputStream jasonStream = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(jasonStream));
				StringBuilder builder = new StringBuilder();
				String in;
				
				while((in = reader.readLine()) != null){
					
					builder.append(in);
					
				}
				
				String jsonData = builder.toString();
				
				Log.v("JASON RESULT", jsonData);
				
				JSONObject jsonO = new JSONObject(jsonData);
				JSONArray series = jsonO.getJSONArray("series");
				JSONObject series_sub = series.getJSONObject(0);
				my_data = series_sub.getJSONArray("data");
	
				//log the value of data for debugging
				//data is an json array containing
				//kwh cost/year values
				String tempData = my_data.toString();
				Log.v("JASON DATA", tempData);
				
				
				//data is an array. find how long the array is
				// and log it for debugging 
			
				
			
				//get and log the first element of the array
				//for debugging
				JSONArray first = my_data.getJSONArray(0);
				String tempFirst = first.toString();
				Log.v("FIRST", tempFirst);
				
				// then get the first string 
				//and print it for debugging.
				kwh_s = first.getString(1);
				Log.v("KWH", kwh_s);

				
				
			
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return kwh_s;
		}
		
		
		protected void onPostExecute(String kwh_r) {
			
			gotData = true;
			
		}	
		
	
	}



	//this onItemSelected is for the spinner
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		// TODO Auto-generated method stub
			stateFinal = parent.getItemAtPosition(pos).toString();
		
	}

	//this is also for spinner
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	
	
	//this is for the gridview's on item click listener
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
		// TODO Auto-generated method stub
		
		v.setSelected(true);
		
	}


	public class ImageAdapter extends BaseAdapter {
	    private Context mContext;

	    public ImageAdapter(Context c) {
	        mContext = c;
	    }

	    public int getCount() {
	        return mThumbIds.length;
	    }

	    public Object getItem(int position) {
	        return null;
	    }

	    public long getItemId(int position) {
	        return 0;
	    }

	    // create a new ImageView for each item referenced by the Adapter
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ImageView imageView;
	     	        
	        if (convertView == null) {  // if it's not recycled, initialize some attributes
	            imageView = new ImageView(mContext);
	            imageView.setLayoutParams(new GridView.LayoutParams(120, 120));
	            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	            imageView.setPadding(8, 8, 8, 8);
	        } else {
	            imageView = (ImageView) convertView;
	        }

	        imageView.setImageResource(mThumbIds[position]);
	        return imageView;
	    }

	    // references to our images
	    private Integer[] mThumbIds = {
	            R.drawable.electricity, R.drawable.naturalgas,
	            R.drawable.gasoline
	       
	    };
	
	}
	
	static class ViewHolder {
		
		ImageView energyPic;
		TextView desc;
		CheckBox checkbox;	
		
	}
	
	static 
	
	
	 @SuppressLint("NewApi")
	public class MultiChoiceModeListener implements GridView.MultiChoiceModeListener {

		@Override
		public boolean onActionItemClicked(ActionMode arg0, MenuItem arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode arg0, Menu arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode arg0, int arg1,
				long arg2, boolean arg3) {
			// TODO Auto-generated method stub
			
		}
	 }
}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*************************
	 * The code below shows the beginning steps
	 * to an alternate method to call an HTTP Web
	 * API. ( the implementation below does not have 
	 * the full code required to call an HTTP Web API).
	 *************************/
	
	/****
	
	private class AccessAPI extends AsyncTask<Void, Void, String> {
        @Override
		protected String doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
        	
        	Object content = null;
        	
        	URL myURL = null;
			try {
				myURL = new URL("http://ZiptasticAPI.com/97213");
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		try {
				content = myURL.getContent();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		return content.toString();
        	
		}
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
        	
    		city.setText(result);

        	
       }
        
        ***/
	
	
	
	
	
	
	
	
	
	

