(ns app.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :as rd]
            [reitit.dev.pretty :as pretty]
            [reitit.ring :as r]))


(defonce server (atom nil))


(defn api-handler [req]
  (prn req)
  {:status 200 :body "OK"})


(defn root-handler [req]
  (prn req)
  {:status 200 :body "index"})


(defn home [{{:keys [a b c]} :path-params}]
  {:status 200 :body (str a b c)}
  )


(def routes
  [[ "/"  {:get root-handler}]
   ["/api" {:get api-handler}]
   ["/home/:a/:b/:c" {:get home}]] )

(comment


  (def hais
    (fn [{a :a b :b c :c}] (+ a b c)))

  (hais {:a 1 :b 2 :c 3})


  (defn a [req]
    ((fn [{{a :a b :b c :c} :a-map}] (+ a b c)) req))

  (a {:a-map {:a 1 :b 2 :c 3}})

  ((fn [{{a :a b :b c :c} :a-map}] (+ a b c)) {:a-map {:a 1 :b 2 :c 3}})

  ((fn [{a :a b :b c :c}] (+ a b c)) {:a 1 :b 2 :c 3})

  (let [{a :a b :b c :c} {:a 1 :b 2 :c 3}] (+ a b c))

  )


(def router
  (r/router routes
    {:exception pretty/exception}))


(def app (r/ring-handler router))


(defn start-jetty! []
  (reset!
   server
   (jetty/run-jetty (rd/wrap-defaults
                     #'app
                     rd/site-defaults)
                    {:join? false
                     :port 4000})))
 

(defn stop-jetty! []
  (.stop @server)
  (reset! server nil))


(defn -main [& _]
  (println "Server started!")
  (start-jetty!))


(defn restart-jetty! []
  (stop-jetty!)
  (-main))


(comment
  (-main))


(comment
  (stop-jetty!))

;; curl -X GET -d hello=world localhost:4000/api
(comment
  (restart-jetty!)
  (app
   {:request-method :get
    :uri "/api"
    :query-params {"x" "2"}}))

(comment
  (restart-jetty!)
  (app
   {:request-method :get
    :uri "/home/1" }))

