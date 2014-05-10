baseliner
=========

Eclipse Plugins to enrich MANIFEST.MF's export packages with computed versions

It automatically updates exported packages versions based on the current API Baseline.

This tool reuses the baseline defined in the Eclipse preferences.

Right now it is using https://github.com/jeluard/semantic-versioning to do the actual computation of diff but that part 
is decoupled through OSGi Declarative Services. This means one could provide another implementation quite easily.

----------

Next steps

* Proper buids and distribution
* per-project configuration to specify namespaces to ignore
* Make sure the micro segment is updated for non-breaking changes.
* automatic rule always export all the packages with the "internal" tag with explicit opt-in.
* automatic upgrade of the bundle version based on the package versions.
* automatic generation and update of an api-change report for each bundle.

