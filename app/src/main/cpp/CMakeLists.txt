cmake_minimum_required(VERSION 3.22.1)

project("commonmsm_native")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Optimization flags for ARM64 and x86_64
add_compile_options(-O3 -funroll-loops -Wall -Wextra)

include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}
)

add_library(
    commonmsm_native
    SHARED
    commonmsm_native.cpp
    moe_streamer.cpp
    llama_wrapper.cpp
)

find_library(
    log-lib
    log
)

find_library(
    android-lib
    android
)

target_link_libraries(
    commonmsm_native
    ${log-lib}
    ${android-lib}
)
