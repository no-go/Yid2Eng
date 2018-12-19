package de.digisocken.yid2eng;

import android.app.UiModeManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static final String PROJECT_LINK = "http://www.cs.uky.edu/~raphael/yiddish/dictionary.cgi";
    public static final String EXPORT_FILENAME = "/wordlist.csv";

    private EntryAdapter entryAdapter;
    private EntryAdapter resultEntryAdapter;
    private ListView entryList;
    private boolean copyPasteWindow;

    private ClipboardManager clipboard;
    public static SharedPreferences mPreferences;
    private TextView emptyView;
    private UiModeManager umm;

    public static int data_total = 1;
    public static int data_line = 0;

    private Handler handler = new Handler();

    private final Runnable updateHintThread = new Runnable() {
        public void run() {
            try {
                ActionBar ab = getSupportActionBar();
                if (ab != null && data_line > 0) {
                    ab.setTitle(String.format(Locale.GERMAN,
                            "  %s %.0f%%",
                            getString(R.string.app_name),
                            (100*(float)data_line/data_total)
                    ));
                    emptyView.setText("");
                    handler.postDelayed(this, 500);
                } else {
                    ab.setTitle("  " + getString(R.string.app_name));
                    emptyView.setText(R.string.empty);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_night:
                if (item.isChecked()) {
                    mPreferences.edit().putBoolean("nightmode", false).apply();
                    umm.setNightMode(UiModeManager.MODE_NIGHT_NO);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    item.setChecked(false);
                } else {
                    mPreferences.edit().putBoolean("nightmode", true).apply();
                    umm.setNightMode(UiModeManager.MODE_NIGHT_YES);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    item.setChecked(true);
                }
                break;
            case R.id.action_copypaste:
                if (item.isChecked()) {
                    copyPasteWindow = false;
                    mPreferences.edit().putBoolean("copypaste", false).apply();
                    item.setChecked(false);
                } else {
                    copyPasteWindow = true;
                    mPreferences.edit().putBoolean("copypaste", true).apply();
                    item.setChecked(true);
                }
                break;
            case R.id.action_project:
                Intent intentProj= new Intent(Intent.ACTION_VIEW, Uri.parse(PROJECT_LINK));
                startActivity(intentProj);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mi1 = menu.findItem(R.id.action_copypaste);
        MenuItem mi2 = menu.findItem(R.id.action_night);
        copyPasteWindow = mPreferences.getBoolean("copypaste", true);
        boolean night = mPreferences.getBoolean("nightmode", false);
        mi1.setChecked(copyPasteWindow);
        mi2.setChecked(night);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_menu, menu);
        super.onCreateOptionsMenu(menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(
        new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                makeToast(getString(R.string.searching, query));
                data_line = 0;
                resultEntryAdapter.filter(query, entryAdapter);
                entryList.setAdapter(resultEntryAdapter);
                resultEntryAdapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { return false;}
        });

        MenuItemCompat.setOnActionExpandListener(
        searchItem,
        new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                entryAdapter.squery = null;
                entryList.setAdapter(entryAdapter);
                entryAdapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                //makeToast(getString(R.string.insertSearch));
                resultEntryAdapter.clear();
                entryList.setAdapter(resultEntryAdapter);
                resultEntryAdapter.notifyDataSetChanged();
                return true;
            }
        });

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setDisplayShowHomeEnabled(true);
                ab.setHomeButtonEnabled(true);
                ab.setDisplayUseLogoEnabled(true);
                ab.setLogo(R.mipmap.ic_launcher);
                ab.setTitle("  " + getString(R.string.app_name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        umm = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        setContentView(R.layout.activity_main);
        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        entryAdapter = new EntryAdapter(this);
        entryList = (ListView) findViewById(R.id.dicList);
        emptyView = (TextView) findViewById(android.R.id.empty);
        entryList.setEmptyView(emptyView);
        entryList.setAdapter(entryAdapter);
        entryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DicEntry item = (DicEntry) adapterView.getItemAtPosition(i);
                String msg = item.title + "\n" + item.yiddish + "\n" + item.body;

                if (copyPasteWindow) {
                    Intent myIntent = new Intent(MainActivity.this, EditActivity.class);
                    //myIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    myIntent.putExtra("msg", msg);
                    startActivity(myIntent);
                } else {
                    ClipData clip = ClipData.newPlainText(
                            getString(R.string.app_name),
                            msg
                    );

                    makeToast(getString(R.string.copying));
                    clipboard.setPrimaryClip(clip);
                }
            }
        });
        handler.postDelayed(updateHintThread, 500);
        resultEntryAdapter = new EntryAdapter(this);


        new RetrieveFeedTask().execute();

    }

    @Override
    protected void onResume() {
        boolean night = mPreferences.getBoolean("nightmode", false);
        if (night) {
            umm.setNightMode(UiModeManager.MODE_NIGHT_YES);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            umm.setNightMode(UiModeManager.MODE_NIGHT_NO);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }
        super.onResume();
    }

    class RetrieveFeedTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... dummy) {

            try {
                //--------------------------------------------- build folder and export file
                File exportedFile;

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    exportedFile = new File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                            getPackageName()
                    );
                } else {
                    exportedFile = new File(Environment.getExternalStorageDirectory() + "/Documents/"+getPackageName());
                }
                String path = exportedFile.getPath() + EXPORT_FILENAME;

                exportedFile.mkdirs();
                exportedFile = new File(path);

                if (!exportedFile.exists()) {
                    exportedFile.createNewFile();

                    //--------------------------------------------- export RAW to the android files
                    InputStream ins = getResources().openRawResource(R.raw.wordlist);
                    Uri datafile = Uri.fromFile(exportedFile);
                    path = PathUtil.getPath(getApplicationContext(), datafile);
                    File file = new File(path);
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buffer = new byte[1024*100];
                    int len;
                    while ((len = ins.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    ins.close();
                    fos.close();
                }

                //--------------------------------------------- read exported file and proccess them
                FileInputStream exportedStream = new FileInputStream(exportedFile);
                String[] str = readTextFile(exportedStream).split(getString(R.string.rowsplit));

                data_total = str.length;
                data_line = 0;
                for (int i=0; i<str.length; i++) {
                    DicEntry dicEntry = new DicEntry();
                    if (str[i].trim().length() == 0) continue;
                    if (str[i].startsWith(getString(R.string.ignoreline))) continue;
                    String[] line = str[i].split(getString(R.string.columnsplit));
                    dicEntry.title = line[0];
                    dicEntry.yiddish = line[2];
                    String eng = str[i].substring(str[i].indexOf("\"")+1, str[i].lastIndexOf("\""));
                    if (eng.length() > 0) {
                        dicEntry.body = line[1] + " · " + line[3] + "\n" + eng;
                    } else {
                        dicEntry.body = line[1] + " · " + line[3];
                    }
                    entryAdapter.addItem(dicEntry);
                    data_line++;
                }
                exportedStream.close();

                entryAdapter.sort();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            data_line = 0;
            entryAdapter.notifyDataSetChanged();
        }
    }

    public void makeToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {}
        return outputStream.toString();
    }
}
