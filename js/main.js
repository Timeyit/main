require.config({

  // alias libraries paths
    paths: {
        'domReady': '../node_modules/requirejs-domready/domReady',
        'angular': '../node_modules/angular/angular'
    },

    // angular does not support AMD out of the box, put it in a shim
    shim: {
        'angular': {
            exports: 'angular'
        }
    },

    // kick start application
    deps: ['./bootstrap']
});