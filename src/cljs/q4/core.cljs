(ns q4.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [q4.ajax :refer [load-interceptors!]]
            [q4.events])
  (:import goog.History))

(defn nav-link [uri title page]
  [:li.nav-item
   {:class (when (= page @(rf/subscribe [:page])) "active")}
   [:a.nav-link {:href uri} title]])

(defn navbar []
  [:nav.navbar.navbar-dark.bg-primary.navbar-expand-md
   {:role "navigation"}
   [:button.navbar-toggler.hidden-sm-up
    {:type "button"
     :data-toggle "collapse"
     :data-target "#collapsing-navbar"}
    [:span.navbar-toggler-icon]]
   [:a.navbar-brand {:href "#/"} "q4"]
   [:div#collapsing-navbar.collapse.navbar-collapse
    [:ul.nav.navbar-nav.mr-auto
     [nav-link "#/" "Home" :home]
     [nav-link "#/about" "About" :about]]]])

(defn add-row-element [vect]
  "Given a vector of [:td _] vectors, this function adds :tr to groups of 7"
  (loop [i       0
         sub-vec []
         new-vec []]
    (if (< i (count vect))
      (if (= 0 (rem i 7))
        (recur (inc i) ^{:key i}[:tr (vect i)] (conj new-vec sub-vec))
        (recur (inc i) (conj sub-vec (vect i)) new-vec))
      (rest (conj new-vec sub-vec)))))

(defn circle [value]
  (case value
    :e [:div]
    :r [:div.red]
    :b [:div.black]))

(defn make-space [space board active?]
  "Given a space and a board, this function creates the HTML for the table cell,
   it's action, and it's value"
  (let [space-value (get-in board space)]
    (vector :td
            (when active?
              {:on-click #(rf/dispatch [:select-column space])})
            (circle space-value))))

(defn table-board []
  "Creates the HTML for the game table that is seen by the players"
  (let [current-board @(rf/subscribe [:board])
        winner?       @(rf/subscribe [:active])
        board-numbers (for [i (range 6)
                            j (range 7)]
                        [i j])
        board-vector (vec (map #(make-space % current-board winner?) board-numbers))]
    [:table>tbody
     (add-row-element board-vector)]))

(defn game-chooser []
  [:div.chooser "What do you want to play?"
   [:div {:on-click #(rf/dispatch [:choose-game :quaint])} "Quaint"]
   [:div {:on-click #(rf/dispatch [:choose-game :quasi])}  "Quasi"]
   [:div {:on-click #(rf/dispatch [:choose-game :quantum])} "Quantum"]])

(defn main-panel []
  (let [chosen? @(rf/subscribe [:game-chosen])
        active? @(rf/subscribe [:active])]
    [:div
     [:h1 "Quantum Four"]
     (if-not chosen?
       [game-chooser chosen?]
       [:div
        [:div.board
         [table-board]]
        [:div.below
         [:p "Turn: "
          (let [turn @(rf/subscribe [:turn])]
            (if (= turn :r)
              "Red"
              "Black"))]
         [:p "Alerts: " @(rf/subscribe [:alert])]
         (if-not active?
           [:p
            {:on-click #(rf/dispatch [:play-again])}
            "Play again"])]])]))

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:img {:src (str js/context "/img/warning_clojure.png")}]]]])

(defn home-page []
  [:div.container
   [main-panel]])

(def pages
  {:home #'home-page
   :about #'about-page})

(defn page []
  [:div
   [navbar]
   [(pages @(rf/subscribe [:page]))]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

(secretary/defroute "/about" []
  (rf/dispatch [:set-active-page :about]))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(rf/dispatch [:set-docs %])}))

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
