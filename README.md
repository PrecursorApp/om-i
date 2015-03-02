# Om-instrumentation

Instrumentation helper for Om applications.

## Overview

Om-i (pronounced "Oh, my!") helps you identify the components in your [Om](https://github.com/omcljs/om) application that are being passed too much of your app state and rendering unnecessarily. It provides useful statistics about render times and frequencies for all of your components.

You can see it live on [Precursor](https://precursorapp.com), a collaborative drawing application. Use Ctrl+Alt+Shift+j to toggle it.

![Demo](http://dtwdl3ecuoduc.cloudfront.net/om-i/instrumentation.gif)

## Setup

### Dependencies
Add Om-i to your project's dependencies

```
[precursor/om-i "0.1.1"]
```

### Enable the instrumentation

Use Om-i's custom descriptor so that it can gather render times for your components. To enable it globally, use the `:instrument` opt in `om/root`

```
(om/root
  app-component
  app-state
  {:target container
   :instrument (fn [f cursor m]
                 (om/build* f cursor
                            (assoc m
                                   :descriptor om-i.core/instrumentation-methods)))})
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
                                   :sort-shorcut #{"ctrl" "alt" "shift" "s"}})
```

### Styles

You need to set up css styles to handle displaying the instrumentation when it's opened. There are sample less and css files in the resources directory.

If you want to try out Om-i, or just use it in development, we've provided a helper that will embed a style tag with the syles from resources/om-i.min.css.

```
(om-i.hacks/insert-styles)
```

It's not recommended to use this in production.

### Wrapping a pre-existing descriptor

If you're already using a custom descriptor, you can still use Om-i. Here's an example wrapping Om's `no-local-descriptor`.

```
(let [methods (om-i.core/instrument-methods om/no-local-state-methods)
      descriptor (om/no-local-descriptor methods)]
  (om/root
    app-component
    app-state
    {:target container
     :instrument (fn [f cursor m]
                   (om/build* f cursor (assoc m :descriptor descriptor)))}))
```

## Acknowledgements

Thanks to [@sgrove](https://github.com/sgrove) for his keyboard handling code. Om-i uses a minimal version of the code he wrote for Precursor. There is an older, [public version of the code in Omchaya](https://github.com/sgrove/omchaya/blob/master/src/omchaya/components/key_queue.cljs).

Thanks to [@brandonbloom](https://github.com/brandonbloom) for demonstrating how to use descriptors in Om. [Related blog post](http://blog.circleci.com/local-state-global-concerns/).

Thanks to [@swannodette](https://github.com/swannodette) for releasing Om.


## License

Copyright © 2015 PrecursorApp

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
