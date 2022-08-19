package com.jaax.retrofitmvp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jaax.retrofitmvp.data.MainMVP
import com.jaax.retrofitmvp.data.model.Result
import com.jaax.retrofitmvp.databinding.ActivityMainBinding
import com.jaax.retrofitmvp.utils.MainConstants
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MainMVP.View {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ResultAdapter
    private lateinit var layoutManager: GridLayoutManager

    @Inject
    lateinit var presenter: MainMVP.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Logger.addLogAdapter(AndroidLogAdapter())

        adapter = ResultAdapter(onPokeListener = { name -> onPokeClicked(name) })
        layoutManager = GridLayoutManager(this, 3)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = layoutManager

        binding.recyclerView.adapter = adapter

        lifecycleScope.launch(Dispatchers.IO) { presenter.getMorePokemon() }
    }

    override fun onResume() {
        super.onResume()

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val itemCount = layoutManager.childCount
                    val itemTotalCount = layoutManager.itemCount
                    val lastItems = layoutManager.findFirstVisibleItemPosition()

                    if (presenter.getMyLoadable()) {
                        if ((itemCount + lastItems) >= itemTotalCount) {
                            presenter.setMyLoadable(false)
                            presenter.increaseOffset(MainConstants.LIMIT)

                            lifecycleScope.launch(Dispatchers.IO) {
                                presenter.getMorePokemon()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun onPokeClicked(name: String) {
        lifecycleScope.launch {
            binding.recyclerView.isClickable = false
            delay(3000)
        }
        PokemonDialog(name).show(supportFragmentManager, "showpokemon")
    }

    override fun showPokemon(listPokemon: List<Result>) {
        adapter.addAllPokemon(listPokemon)
    }
}