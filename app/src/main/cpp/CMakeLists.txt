cmake_minimum_required(VERSION 3.4.1)

project("native-lib" VERSION 1.0.0)

if(DEFINED GOOGLE_API_KEY_HEX)
    add_compile_definitions(GOOGLE_API_KEY_HEX="${GOOGLE_API_KEY_HEX}")
else()
    message(WARNING "GOOGLE_API_KEY_HEX is not defined")
endif()

add_library( native-lib
        SHARED
        native-lib.cpp )

find_library( log-lib
        log )

target_link_libraries( native-lib
        ${log-lib} )
