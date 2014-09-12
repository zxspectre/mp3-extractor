    function [handles] = scatter_series_set(x_vals, y_vals, sizes, colors, styles)

        N = length(x_vals);

        if ( (~ ( N == length(y_vals))) || (~ ( N == length(sizes)))  || ...
             (~ ( N == length(colors))) || (~ ( N == length(styles))) )
            error('scatter_series_set: all arguments must be cell arrays of the same length');
        end

        %plot the first series
        handles = cell([N, 1]);
        handles{1} = plot(x_vals{1}, y_vals{1});
        set(handles{1}, 'linestyle', 'none');
        set(handles{1}, 'marker', styles{1});
        set(handles{1}, 'markersize', sizes{1});
        set(handles{1}, 'color', colors{1});

        %plot additional series if present
        if N > 1
            hold on;
            for ind = 2:N
                handles{ind} = plot(x_vals{ind}, y_vals{ind});
                set(handles{ind}, 'linestyle', 'none');
                set(handles{ind}, 'marker', styles{ind});
                set(handles{ind}, 'markersize', sizes{ind});
                set(handles{ind}, 'color', colors{ind});
            end
            hold off;
        end
    end