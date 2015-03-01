# Om-instrumentation

Instrumentation helper for Om applications.

## Overview

Om-i (pronounced "Oh, my!") helps you identify the components in your Om application that are being passed too much of your app state and rendering unnecessarily. It provides useful statistics about render times and frequencies for all of your components.

Here it is in action:

![Demo](http://dtwdl3ecuoduc.cloudfront.net/om-i/om-i-demo.gif)

You can see it live on [https://precursorapp.com](Precursor), a collaborative drawing application. Use Ctrl+Alt+Shift+j to toggle it.

## Setup

### Dependencies
Add Om-i to your project's dependencies

```
[precursor/om-i "0.1.0-SNAPSHOT"]
```

### Enable the instrumentation

Use Om-i's custom descriptor so that it can gather render times for your components. To enable it globally, use the `:instrument` opt in `om/root`

```
(om/root app-component
         app-state
         {:target container
          :instrument (fn [f cursor m]
                        (om/build* f cursor (assoc m :descriptor om-i.core/instrumentation-methods)))})
```

### Mount the component

Add the following somewhere in your setup code. If you're using figwheel, place it somewhere that won't get reloaded.

```
(om-i.core/setup-component-stats!)
```

Om-i renders its statistics in a separate root so that it doesn't interact with your application.

It will create a `div` in the body with classname "om-instrumentation" by default and assign three keyboard shortcuts: Ctrl+Alt+Shift+j to bring down the statistics menu, Ctrl+Alt+Shift+k to clear the statistics, and Ctrl+Alt+Shift+s to switch the sort order.


You can override the defaults with:

```
(om-i.core/setup-component-stats! {:class "om-instrumentation"
                                   :clear-shortcut #{"ctrl" "alt" "shift" "j"}
                                   :toggle-shortcut #{"ctrl" "alt" "shift" "k"}
                                   :sort-shorcut #{"ctrl" "alt" "shift" "s"}}})
```

### Styles

You need to set up css styles to handle displaying the instrumentation when it's opened. There are sample less and css files in the resources directory.

To try out Om-i, or just using it in development, we've provided a helper that will embed a style tag with the syles from resources/om-i.min.css.

```
(om-i.hacks/insert-styles)
```

It's not recommended to use this in production.

## License

Copyright © 2015 PrecursorApp

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
