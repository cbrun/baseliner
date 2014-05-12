baseliner
=========

Eclipse Plugins to enrich MANIFEST.MF's export packages with computed versions

It automatically updates exported packages versions based on the current API Baseline.
This tool reuses the baseline defined in the Eclipse preferences.

If no package version was specified, the current bundle-version is taken.


Right now it is using https://github.com/jeluard/semantic-versioning to do the actual computation of diff but that part 
is decoupled through OSGi Declarative Services. This means one could provide another implementation quite easily.


----------

Giving it a try :
- install it in your Eclipse IDE, update site is here : http://marketplace.obeonetwork.com/updates/nightly/baseliner/repository
- Right click on a project then click on the action "Enable/Disable Automatic Versioning"
- Set an API Baseline if none is set yet : http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.pde.doc.user%2Ftasks%2Fapi_tooling_baseline.htm
- Enjoy


----------

Next steps

* Proper buids and distribution
* per-project configuration to specify namespaces to ignore
* Make sure the micro segment is updated for non-breaking changes.
* automatic rule always export all the packages with the "internal" tag with explicit opt-in.
* automatic upgrade of the bundle version based on the package versions.
* automatic generation and update of an api-change report for each bundle.

----------

The code is pretty much in a draf state right now, what is notably missing :

* Review of naming in the APIs
* Proper error handling and logging
* Proper target platform definition.
* Import-Package versioning ;)
