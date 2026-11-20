package com.krishang.evroute;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class MainActivity extends Activity {
    private static final List<Router.Road> ROADS = List.of(
            new Router.Road("Start", "Pine Junction", 100),
            new Router.Road("Pine Junction", "Destination", 50));

    private SeekBar charge;
    private Switch station;
    private TextView result;
    private TextView chargeLabel;

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        var scroll = new ScrollView(this);
        var column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        int padding = Math.round(24 * getResources().getDisplayMetrics().density);
        column.setPadding(padding, padding, padding, padding);
        scroll.setClipToPadding(false);
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            var bars = insets.getInsets(android.view.WindowInsets.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        scroll.addView(column);
        setContentView(scroll);

        var title = new TextView(this);
        title.setText("Offline EV Router");
        title.setTextSize(30);
        column.addView(title);
        var details = new TextView(this);
        details.setText("150 km to Destination\n60 kWh battery, 200 Wh/km, 10% arrival reserve");
        column.addView(details);
        chargeLabel = new TextView(this);
        column.addView(chargeLabel);
        charge = new SeekBar(this);
        charge.setMax(100);
        charge.setProgress(saved == null ? 50 : saved.getInt("charge"));
        charge.setContentDescription("Starting charge");
        column.addView(charge);
        station = new Switch(this);
        station.setText("Pine Junction charger available");
        station.setChecked(saved == null || saved.getBoolean("station"));
        column.addView(station);
        result = new TextView(this);
        result.setTextSize(18);
        column.addView(result);
        charge.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                render();
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {}
        });
        station.setOnCheckedChangeListener((button, checked) -> render());
        render();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt("charge", charge.getProgress());
        out.putBoolean("station", station.isChecked());
    }

    private void render() {
        chargeLabel.setText(String.format(Locale.US, "Starting charge: %d%%", charge.getProgress()));
        Set<String> stations = station.isChecked() ? Set.of("Pine Junction") : Set.of();
        var battery = new Router.Battery(60_000, charge.getProgress() * 600, 6_000, 200);
        var route = Router.plan(ROADS, stations, "Start", "Destination", battery);
        if (!route.isPresent()) {
            result.setText("No route meets the 10% arrival reserve.");
            return;
        }
        StringBuilder summary = new StringBuilder("Route: ").append(route.get().km()).append(" km");
        for (Router.Step step : route.get().steps()) {
            summary.append(String.format(Locale.US, "\n%s%s: %.1f kWh", step.location(),
                    step.charged() ? " (charged)" : "", step.remainingWh() / 1000.0));
        }
        result.setText(summary);
    }
}
