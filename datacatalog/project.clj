(defproject datacatalog "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.2.1"]
                 [ring/ring-jetty-adapter "1.2.1"]
                 [compojure "1.2.0-SNAPSHOT"]
                 [joda-time/joda-time "2.3"]
                 [org.clojure/tools.reader "0.7.5"]
                 [org.clojure/data.json "0.2.3"]
                 ]




  :plugins [[lein-ring "0.8.7"]]

  :aot [datacatalog.core]

  :uberjar-name "datacatalog-full.jar"
  :ring {:handler datacatalog.core/app
         :init datacatalog.core/load!}

  )
