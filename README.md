# Offline EV route planner

Choose charging stops from an offline road graph using battery capacity, consumption, and minimum arrival reserve.

## First acceptance test

For a 60 kWh battery starting at 50%, a 10% reserve and consumption of 0.2 kWh/km, a 150 km route requires a charge. A working station at 100 km makes the route feasible. Disabling it should produce a clear no-route result.

The app ships with a simulated, directed road graph. Toggle Pine Junction's charger
or adjust starting charge to compare routes. The planner searches for the shortest
feasible distance, breaking distance ties by using fewer charging stops. A working
station can refill the battery to full. The reserve applies at the destination.
Road distance and consumption are fixed in this demo.

Use JDK 17 or 21 and Android SDK Platform 35 with Build Tools 34.0.0.
Run `gradlew.bat :app:testDebugUnitTest :app:assembleDebug` on Windows
or `./gradlew :app:testDebugUnitTest :app:assembleDebug` on macOS or Linux.
The debug APK supports Android 12 (API 31) and newer.

The JVM tests cover charger outages, an exact reserve, a station reached with an
empty battery, a feasible detour, an empty graph, invalid inputs and large distances.
