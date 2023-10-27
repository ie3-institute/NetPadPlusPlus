# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased/Snapshot]

### Changed
-   BREAKING: replaced [Unit API 1.0](https://github.com/unitsofmeasurement/uom-se) (JSR 363, tec.uom.se) with [Unit API 2.0](https://github.com/unitsofmeasurement/indriya) (JSR 385, tech.units.indriya)
-   Removed RocketChat notifications from Jenkinsfile [#181](https://github.com/ie3-institute/NetPadPlusPlus/issues/181)
-   Update to Gradle 8 [#242](https://github.com/ie3-institute/NetPadPlusPlus/issues/242)

## **0.1.0** - 2020-11-05

### Added
-   Basic input and output handling for grid models provided by csv files following the specifications of [PowerSystemDataModel](https://raw.githubusercontent.com/ie3-institute/PowerSystemDataModel) - up to version 1.3.2
-   Visualisation of the grid model including system participants (e.g. loads, pv plants, ...)
-   Basic editing functionality (Renaming, moving around nodes to alter their geographical position, ...)
