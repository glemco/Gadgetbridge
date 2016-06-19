package nodomain.freeyourgadget.gadgetbridge.activities.appmanager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.AbstractFragmentPagerAdapter;
import nodomain.freeyourgadget.gadgetbridge.activities.AbstractGBFragmentActivity;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.util.FileUtils;


public class AppManagerActivity extends AbstractGBFragmentActivity {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractAppManagerFragment.class);

    private GBDevice mGBDevice = null;

    public GBDevice getGBDevice() {
        return mGBDevice;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragmentappmanager);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mGBDevice = extras.getParcelable(GBDevice.EXTRA_DEVICE);
        } else {
            throw new IllegalArgumentException("Must provide a device when invoking this activity");
        }


        // Set up the ViewPager with the sections adapter.
        ViewPager viewPager = (ViewPager) findViewById(R.id.appmanager_pager);
        viewPager.setAdapter(getPagerAdapter());
    }

    @Override
    protected AbstractFragmentPagerAdapter createFragmentPagerAdapter(FragmentManager fragmentManager) {
        return new SectionsPagerAdapter(fragmentManager);
    }

    public static synchronized void deleteFromAppOrderFile(String filename, UUID uuid) {
        ArrayList<UUID> uuids = getUuidsFromFile(filename);
        uuids.remove(uuid);
        rewriteAppOrderFile(filename, uuids);
    }

    public class SectionsPagerAdapter extends AbstractFragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    return new AppManagerFragmentCache();
                case 1:
                    return new AppManagerFragmentInstalledApps();
                case 2:
                    return new AppManagerFragmentInstalledWatchfaces();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Apps in cache";
                case 1:
                    return "Installed apps";
                case 2:
                    return "Installed watchfaces";
                case 3:
            }
            return super.getPageTitle(position);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    static synchronized void rewriteAppOrderFile(String filename, List<UUID> uuids) {
        try {
            FileWriter fileWriter = new FileWriter(FileUtils.getExternalFilesDir() + "/" + filename);
            BufferedWriter out = new BufferedWriter(fileWriter);
            for (UUID uuid : uuids) {
                out.write(uuid.toString());
                out.newLine();
            }
            out.close();
        } catch (IOException e) {
            LOG.warn("can't write app order to file!");
        }
    }

    synchronized public static void addToAppOrderFile(String filename, UUID uuid) {
        ArrayList<UUID> uuids = getUuidsFromFile(filename);
        if (!uuids.contains(uuid)) {
            uuids.add(uuid);
            rewriteAppOrderFile(filename, uuids);
        }
    }

    static synchronized ArrayList<UUID> getUuidsFromFile(String filename) {
        ArrayList<UUID> uuids = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(FileUtils.getExternalFilesDir() + "/" + filename);
            BufferedReader in = new BufferedReader(fileReader);
            String line;
            while ((line = in.readLine()) != null) {
                uuids.add(UUID.fromString(line));
            }
        } catch (IOException e) {
            LOG.warn("could not read sort file");
        }
        return uuids;
    }
}
