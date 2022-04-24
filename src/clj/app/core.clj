(ns app.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [reitit.dev.pretty :as pretty]
            [ring.util.response :refer [redirect]]
            [ring.middleware.oauth2 :refer [wrap-oauth2]]
            [ring.middleware.reload :refer [wrap-reload]]
            [environ.core :refer [env]]
            [reitit.ring :as r]))


(def config
  {:github-client-id "e94c21eaeb852d237790"
   :github-client-secret "d3e18d6edf8d85c37679c5c934d04892b45e37e9"})


(defonce server (atom nil))


(defn get-api [req]
  (prn req)
  {:status 200 :body "OK"})


(defn get-root [req]
  (prn req)
  {:status 200 :body "index"})


(defn get-login [_]
  (prn (:github-auth-page config) )
  {redirect(:github-auth-page config)})


(defn get-home [{{:keys [a b c]} :path-params}]
  {:status 200 :body (str a b c)})


(def routes
  [[ "/"  {:get get-root}]
   ["/login" {:get get-login}]
   ["/api" {:get get-api}]
   ["/home/:a/:b/:c" {:get get-home}]] )


(def github-spec
  {:github {:authorize-uri    "https://github.com/login/oauth/authorize"
            :access-token-uri "https://github.com/login/oauth/access_token"
            :client-id        (:github-client-id config)
            :client-secret    (:github-client-secret config)
            :scopes           ["user:email"]
            :launch-uri       "/oauth2/github"
            :redirect-uri     "/oauth2/github/callback"
            :landing-uri      "/landing"}})


(def my-site-defaults
  (-> site-defaults (assoc-in [:session :cookie-attrs :same-site] :lax)))


(def router
  (r/router (-> routes
                (wrap-oauth2 github-spec)
                (wrap-defaults my-site-defaults))
            
            {:exception pretty/exception}))


(def app (r/ring-handler router))


(defn start-jetty! []
  (reset!
   server
   (jetty/run-jetty (wrap-defaults
                     #'app
                     site-defaults)
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
    :uri "/login" }))


(comment

  (def hais
    (fn [{a :a b :b c :c}] (+ a b c)))

  (hais {:a 1 :b 2 :c 3})

  (defn a [req]
    ((fn [{{a :a b :b c :c} :a-map}] (+ a b c)) req))

  (a {:a-map {:a 1 :b 2 :c 3}})

  ((fn [{{a :a b :b c :c} :a-map}] (+ a b c)) {:a-map {:a 1 :b 2 :c 3}})

  ((fn [{a :a b :b c :c}] (+ a b c)) {:a 1 :b 2 :c 3})

  (let [{a :a b :b c :c} {:a 1 :b 2 :c 3}] (+ a b c)))


