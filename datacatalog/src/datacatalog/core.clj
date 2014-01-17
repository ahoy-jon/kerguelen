(ns datacatalog.core
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.data.json :as json]
            [clojure.pprint :as pprint]))


(defn- read-one
  [r]
  (try
    (read r)
    (catch java.lang.RuntimeException e
      (if (= "EOF while reading" (.getMessage e))
        ::EOF
        (throw e)))))

(defn read-seq-from-file
  "Reads a sequence of top-level objects in file at path."
  [path]
  (with-open [r (java.io.PushbackReader. (clojure.java.io/reader path))]
    (binding [*read-eval* false]
      (doall (take-while #(not= ::EOF %) (repeatedly #(read-one r)))))))


(defn append-to-file [path struc]
  (spit path (prn-str struc) :append true))


(def db (atom '()))

(defn save! [struc]
   (swap! db (fn [atmc]
               (append-to-file "data.clj" struc)
               (cons struc atmc))))

(defn load! []
  (if (.exists (clojure.java.io/as-file "data.clj"))
  (reset! db (read-seq-from-file "data.clj"))))



#_ (save!  {"description" "I AM FREEEEEEEE",
  "path" "/bi/sqoop/plouf",
  "end" "1389968050",
  "start" "1389968050",
  "name" "viaduc.Member"})


(use 'compojure.core)


(defn long->date [l]
  (java.util.Date. (long  (* 1000 (Long/parseLong l)))))

(long->date "1389968050")

(defn keyworkize-map [m]
  (into {} (for [[k v] m] [(keyword k) v])))

(defn clean-data [p]
  (-> p
      keyworkize-map
      (update-in [:end] long->date)
      (update-in [:start] long->date)))


#_ (clean-data {"description" "I AM FREEEEEEEE",
  "path" "/bi/sqoop/plouf",
  "end" "1389968050",
  "start" "1389968050",
  "name" "viaduc.Member"})

(keyworkize-map {"name" ["viaduc.Connexion" "viaduc.Member"], "from" "1389968050"})

(defn force-vec [v]
  (if (seq? v) v [v]))

(defn find-dataset [{:keys [name from]}]
  (let [from (long->date from)
        names (force-vec name)
        dts   (group-by :name (filter   (fn [{s :start n :name}]
                (not= -1 (compare s from))) (filter :start @db) ))]

    (if (empty? (remove (set (keys dts)) names))
       dts)))


#_ (save! :plouf)

#_ (save! {:start #inst "2014-01-16T14:14:10.000-00:00"})

#_ (find-dataset  {:name ["viaduc.Connexion" "viaduc.Member"], :from "1389968050"})

#_ (find-dataset {:name "viaduc.Member", :from "1389967060"})


(extend-protocol json/JSONWriter
  java.util.Date
  (-write [object out] (.print out (str (/ (.getTime object) 1000)))))


(defroutes all-routes
  (POST "/catalog" {params :params}  (let [c-d (doall (clean-data params))] (do
                                         (save! c-d)
                                         (str c-d))))
  (GET  "/catalog" {params :query-params}
        (let [df (find-dataset (keyworkize-map params))]
          (if df (json/write-str  df))))

  (GET "/fullcatalog" [] (json/write-str @db))
  )



(defn inspect [handler]
  (fn [request]
    (pprint/pprint (dissoc request :body))
    (handler request)
    )
  )


(def app (handler/site (inspect all-routes)))


#_ (do  (use 'ring.adapter.jetty)
  (defonce server (run-jetty #'app {:port 8080 :join? false})))








#_ (def sample-post-query

{:ssl-client-cert nil,
 :remote-addr "0:0:0:0:0:0:0:1",
 :scheme :http,
 :query-params {},
 :session {},
 :form-params
 {"description" "I AM FREEEEEEEE",
  "path" "/bi/sqoop/plouf",
  "end" "1389968050",
  "start" "1389968050",
  "name" "viaduc.Member"},
 :multipart-params {},
 :request-method :post,
 :query-string nil,
 :content-type "application/x-www-form-urlencoded",
 :cookies {},
 :uri "/catalog",
 :session/key nil,
 :server-name "localhost",
 :params
 {:description "I AM FREEEEEEEE",
  :path "/bi/sqoop/plouf",
  :end "1389968050",
  :start "1389968050",
  :name "viaduc.Member"},
 :headers
 {"origin" "chrome-extension://hgmloofddffdnphfgcellkdfbfbjeloo",
  "accept-encoding" "gzip,deflate,sdch",
  "connection" "keep-alive",
  "user-agent"
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.77 Safari/537.36",
  "accept-language" "en-US,en;q=0.8,fr;q=0.6",
  "content-type" "application/x-www-form-urlencoded",
  "content-length" "105",
  "accept" "*/*",
  "host" "localhost:8080"},
 :content-length 105,
 :server-port 8080,
 :character-encoding nil,
 :flash nil})





#_ (def sample-get-query {:ssl-client-cert nil,
 :remote-addr "0:0:0:0:0:0:0:1",
 :scheme :http,
 :query-params
 {"name" ["viaduc.Connexion" "viaduc.Member"], "from" "1389968050"},
 :session {},
 :form-params {},
 :multipart-params {},
 :request-method :get,
 :query-string
 "from=1389968050&name=viaduc.Connexion&name=viaduc.Member",
 :content-type "text/plain; charset=UTF-8",
 :cookies {},
 :uri "/catalog",
 :session/key nil,
 :server-name "localhost",
 :params {:name "viaduc.Member", :from "1389968050"},
 :headers
 {"accept-encoding" "gzip,deflate,sdch",
  "connection" "keep-alive",
  "user-agent"
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.77 Safari/537.36",
  "accept-language" "en-US,en;q=0.8,fr;q=0.6",
  "content-type" "text/plain; charset=UTF-8",
  "accept" "*/*",
  "host" "localhost:8080"},
 :content-length nil,
 :server-port 8080,
 :character-encoding "UTF-8",
 :flash nil})

#_ (all-routes sample-post-query)



