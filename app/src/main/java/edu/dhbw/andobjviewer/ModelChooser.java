package edu.dhbw.andobjviewer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import edu.dhbw.andarmodelviewer.R;


import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.skylion.speech.Narrator;
import com.skylion.speech.Synthesiser;

/**
 * allows you to choose either one of the internal models
 * or choose an file on the sd card through another activity
 * @author Tobias Domhan
 *
 */
public class ModelChooser extends ListActivity {
    public static Narrator narrator;
    public static String[] excerptText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AssetManager am = getAssets();
		Vector<Item> models = new Vector<Item>();
		Item item = new Item();
		item.text = getResources().getString(R.string.select_model_file);
		item.type = Item.TYPE_HEADER;
		models.add(item);

        try {
			String[] modelFiles = am.list("models");
			List<String> modelFilesList = Arrays.asList(modelFiles);
			for (int i = 0; i < modelFiles.length; i++) {
				String currFileName = modelFiles[i];
				if(currFileName.endsWith(".obj")) { 
					item = new Item();
					String trimmedFileName = currFileName.substring(0,currFileName.lastIndexOf(".obj"));
                    item.text = trimmedFileName;
                    models.add(item);
					if(modelFilesList.contains(trimmedFileName+".jpg")) {
						InputStream is = am.open("models/"+trimmedFileName+".jpg");
						item.icon=(BitmapFactory.decodeStream(is));
					} else if(modelFilesList.contains(trimmedFileName+".png")) {
						InputStream is = am.open("models/"+trimmedFileName+".png");
						item.icon=(BitmapFactory.decodeStream(is));
					} 
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		item = new Item();
		item.text = getResources().getString(R.string.custom_model);
		item.type = Item.TYPE_HEADER;
		models.add(item);
		item = new Item();
		item.text = getResources().getString(R.string.custom_model_file);
		item.icon = R.drawable.open;
		models.add(item);
		item = new Item();
		item.text = getResources().getString(R.string.help);
		item.type = Item.TYPE_HEADER;
		models.add(item);
		item = new Item();
		item.text = getResources().getString(R.string.instructions);
		item.icon = R.drawable.help;
		models.add(item);
		
		/*setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, models));*/
		
		setListAdapter(new ModelChooserListAdapter(models));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
        if(excerptText == null) {
            List<String> lineList;
            String line;
            BufferedReader reader;
            try {
                lineList = new ArrayList<String>();
                reader = new BufferedReader(new InputStreamReader(getAssets().open("excerpt.txt")));

                while((line = reader.readLine()) != null)
                    lineList.add(line);

                excerptText = lineList.toArray(new String[lineList.size()]);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		Item item = (Item) this.getListAdapter().getItem(position);
		String str = item.text;
		if(str.equals(getResources().getString(R.string.select_model_file))) {
			//start oi file manager activity
			Intent intent = new Intent(ModelChooser.this, CheckFileManagerActivity.class);
            startActivity(intent);
		} else if(str.equals(getResources().getString(R.string.instructions))) {
			//show the instructions activity
			startActivity(new Intent(ModelChooser.this, InstructionsActivity.class));
		} else {
			//load the selected internal file
            final String lang = excerptText[0].substring(excerptText[0].lastIndexOf(' '));
            int num = 0;
            switch(num = Integer.parseInt(str.substring("Scene".length()))) {
                case 1:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            speak(lang, excerptText[1]);
                        }
                    }).start();
                    break;
                case 2:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            speak(lang, excerptText[2]);
                        }
                    }).start();
                    break;
                case 3:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            speak(lang, excerptText[3]);
                        }
                    }).start();
                    break;
            }

			Intent intent = new Intent(ModelChooser.this, AugmentedModelViewerActivity.class);
            intent.putExtra("name", str+".obj");
            intent.putExtra("type", AugmentedModelViewerActivity.TYPE_INTERNAL);
            intent.setAction(Intent.ACTION_VIEW);
            startActivity(intent);
		}
	}

    private void speak(String language, String text){
        Synthesiser synth = new Synthesiser(language);
        InputStream is;
        try {
            is = synth.getMP3Data(text);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if(narrator != null)
            narrator.stop();
        narrator = new Narrator(is);
        narrator.start();
    }
	
	class ModelChooserListAdapter extends BaseAdapter{
		
		private Vector<Item> items;
		
		public ModelChooserListAdapter(Vector<Item> items) {
			this.items = items;
		}
		

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public int getViewTypeCount() {
			//normal items, and the header
			return 2;
		}
		
		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}
		
		@Override
		public boolean isEnabled(int position) {
			return !(items.get(position).type==Item.TYPE_HEADER);
		}
		
		@Override
		public int getItemViewType(int position) {
			return items.get(position).type;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			Item item = items.get(position);
            if (v == null) {
            	LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            	switch(item.type) {            	
            	case Item.TYPE_HEADER:            		
                    v = vi.inflate(R.layout.list_header, null);
            		break;
            	case Item.TYPE_ITEM:
            		v = vi.inflate(R.layout.choose_model_row, null);
            		break;
            	}                
            }   
            if(item != null) {
	            switch(item.type) {            	
	        	case Item.TYPE_HEADER: 
	        		TextView headerText = (TextView) v.findViewById(R.id.list_header_title);
	        		if(headerText != null) {
	        			headerText.setText(item.text);
	        		}
	        		break;
	        	case Item.TYPE_ITEM:
	        		Object iconImage = item.icon;
	            	ImageView icon = (ImageView) v.findViewById(R.id.choose_model_row_icon);
	            	if(icon!=null) {
	            		if(iconImage instanceof Integer) {
	            			icon.setImageResource(((Integer)iconImage).intValue());
	            		} else if(iconImage instanceof Bitmap) {
	            			icon.setImageBitmap((Bitmap)iconImage);
	            		}
	            	}
	            	TextView text = (TextView) v.findViewById(R.id.choose_model_row_text);
	            	if(text!=null)
	            		text.setText(item.text);   
	        		break;
	        	}      
            }
			return v;
		}
		
	}
	
	class Item {
		public static final int TYPE_ITEM=0;
		public static final int TYPE_HEADER=1;
		public int type = TYPE_ITEM;
		public Object icon = new Integer(R.drawable.missingimage);
		public String text;
	}
	
}
