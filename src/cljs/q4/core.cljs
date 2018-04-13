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
    (vector :td.board-cell
            (when active?
              {:on-click #(rf/dispatch [:select-column space])
               :on-mouse-over #(rf/dispatch [:hover-column space])})
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

(def hover-table
  [:tr
   [:td.hover-cell][:td.hover-cell][:td.hover-cell][:td.hover-cell][:td.hover-cell][:td.hover-cell][:td.hover-cell]])

(defn make-hover-cell
  "Takes a cell of the hover table. If the game is active, it adds the color div to the cell.
  If not active, returns cell"
  [cell]
  (let [color  @(rf/subscribe [:turn])
        active @(rf/subscribe [:active])]
    (if active
      (conj cell (circle color))
      cell)))

(defn hover []
  (let [[hov-row hov-col] @(rf/subscribe [:hover-cell])
        active            @(rf/subscribe [:active])]
    [:div
     [:table {:style {:margin "auto"}}
      (update hover-table (inc hov-col) make-hover-cell)]]))

(defn game-chooser []
  [:div.chooser "What do you want to play?"
   [:div {:on-click #(rf/dispatch [:choose-game :quaint])} "Quaint"]
   [:div {:on-click #(rf/dispatch [:choose-game :quasi])}  "Quasi"]
   [:div {:on-click #(rf/dispatch [:choose-game :quantum])} "Quantum"]])

(defn bg-perp []
  [:div.perp
   [:div.top-row
    [:div.top-left-out
     [:div.top-left-in]]
    [:div.top-right-out
     [:div.top-right-in]]]
   [:div.bottom-row
    [:div.bottom-left-out
     [:div.bottom-left-in]]
    [:div.bottom-right-out
     [:div.bottom-right-in]]]])

(defn main-panel []
  (let [chosen? @(rf/subscribe [:game-chosen])
        active? @(rf/subscribe [:active])]
    [:div
     [bg-perp]
     [:div.main-page
      [:h1 {:style {:font-size "100px"}} "Quantum Four"]
      (if-not chosen?
        [game-chooser chosen?]
        [:div
         [hover]
         [:div.board
          [table-board]]
         [:div.below
          [:p.game-notes "Turn: "
           (let [turn @(rf/subscribe [:turn])]
             (if (= turn :r)
               "Red"
               "Black"))]
          [:p.game-notes "Alerts: " @(rf/subscribe [:alert])]
          (if-not active?
            [:p.game-notes {:on-click #(rf/dispatch [:play-again])}
             "Play again"])]])]]))

(defn home-page []
  [:div
   [main-panel]])

(def pages
  {:home #'home-page})

(defn page []
  [:div
   [(pages @(rf/subscribe [:page]))]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

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
