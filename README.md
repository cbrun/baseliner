baseliner
=========

This plugin is a prototype to evaluate the following :
- semantic versionning can be introduced at development time in a large scale team with no effort
- it can be introduced with no friction regarding existing processes and technologies
- the technologies which have around makes this quite cheap to setup

In this spirit this tooling final goal is :
- apply semantic versioning rules on export-packages automatically
- no performance overhead at development time
- the capability to setup and share a baseline description for a whole team (no manual step⁾
- provide awareness of the changes compatibility for the developper
- do not replace existing toolchains like Tycho or PDE.

Eclipse Plugins to enrich MANIFEST.MF's export packages with computed versions

It automatically updates exported packages versions based on the current API Baseline.
This tool reuses the baseline defined in the Eclipse preferences.

If no package version was specified, the current bundle-version is taken.

Right now it is using BND (https://github.com/bndtools/bnd) to do the actual computation of semantic version to use

Medium term, the PDE baseline will not be used anymore as it is really inconvenient to deploy and update within a team.

----------

Giving it a try :
- install it in your Eclipse IDE, update site is here : http://marketplace.obeonetwork.com/updates/nightly/baseliner/repository
- Set an API Baseline if none is set yet : http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.pde.doc.user%2Ftasks%2Fapi_tooling_baseline.htm
- Right click on a project then click on the action "Configure/baseline projects"
- Enjoy

An incremental builder is available too but even if it's quite fast compared to the PDE api tooling, it could still be improved quite a lot
on large project once I have the proper APIs in BNDlib.


----------

Next steps

* ~~Proper buids and distribution~~
* per-project configuration to specify namespaces to ignore
* ~~trigger ManifestCleanup and keep project-specific settings~~ : It's actually too slow and bloat the Manifest.MF
* Make sure the micro segment is updated for non-breaking changes.
* automatic rule always export all the packages with the "internal" tag with explicit opt-in.
* ~~automatic upgrade of the bundle version based on the package versions.~~
* automatic generation and update of an api-change report for each bundle.
* ~~Avoid the self triggering of the builder when Manifest.MF is changed by the builder~~
* Profile and assess performance overhead when 100 projects are in the workspace
* Self provisionning of API baselines based on per-project configuration.
* ~~a mean to use the tool without using the incremental builder approach~~
* ~~keep pom.xml in sync with bundle-version~~



----------

The code is pretty much in a draf state right now, what is notably missing :

* Review of naming in the APIs.
* ~~The builder is not filtering deltas, it will be triggered whatever change happen, N times for N projects.~~
* Proper error handling and logging.
* ~~Proper target platform definition.~~
* Use the target platform definition in the CI build
* Import-Package versioning ;)
