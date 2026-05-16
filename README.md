# Offline EV route planner

Choose charging stops from an offline road graph using battery capacity, consumption, and minimum arrival reserve.

## First acceptance test

For a 60 kWh battery starting at 50%, a 10% reserve and consumption of 0.2 kWh/km, a 150 km route requires a charge. A working station at 100 km makes the route feasible. Disabling it should produce a clear no-route result.